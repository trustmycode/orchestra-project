package com.orchestra.api.service;

import com.orchestra.api.config.RabbitMQConfig;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.ScenarioDependency;
import com.orchestra.domain.model.SuiteRun;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestStepResult;
import com.orchestra.domain.repository.SuiteRunRepository;
import com.orchestra.domain.repository.TestRunRepository;
import com.orchestra.domain.repository.TestStepResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuiteRunOrchestrator {

    private final SuiteRunRepository suiteRunRepository;
    private final TestRunRepository testRunRepository;
    private final TestStepResultRepository testStepResultRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;

    private static final Set<String> TERMINAL_STATUSES = Set.of("PASSED", "FAILED", "FAILED_STUCK", "CANCELLED",
            "SKIPPED");
    private static final Set<String> FAILED_STATUSES = Set.of("FAILED", "FAILED_STUCK", "CANCELLED");

    @Scheduled(fixedDelay = 5000)
    public void orchestrate() {
        // 1. Activate PENDING suites
        List<SuiteRun> pendingSuites = suiteRunRepository.findByStatus("PENDING");
        for (SuiteRun suite : pendingSuites) {
            transactionTemplate.executeWithoutResult(status -> {
                log.info("Starting SuiteRun {}", suite.getId());
                suite.setStatus("IN_PROGRESS");
                suite.setStartedAt(OffsetDateTime.now());
                suiteRunRepository.save(suite);
            });
        }

        // 2. Process IN_PROGRESS suites
        List<SuiteRun> activeSuites = suiteRunRepository.findByStatus("IN_PROGRESS");
        for (SuiteRun suite : activeSuites) {
            try {
                processSuite(suite);
            } catch (Exception e) {
                log.error("Error processing suite {}", suite.getId(), e);
            }
        }
    }

    private void processSuite(SuiteRun suite) {
        List<String> runsToQueue = new ArrayList<>();

        transactionTemplate.executeWithoutResult(status -> {
            // Reload suite to ensure we have the latest state in this transaction
            SuiteRun currentSuite = suiteRunRepository.findById(suite.getId()).orElseThrow();
            List<TestRun> runs = testRunRepository.findBySuiteRunId(currentSuite.getId());

            boolean isSequential = "SEQUENTIAL".equals(currentSuite.getRunMode());

            // Map key -> run for dependency lookup
            Map<String, TestRun> runsByKey = runs.stream()
                    .collect(Collectors.toMap(r -> r.getScenario().getKey(), r -> r));

            // Collect context from finished runs to support data dependencies
            Map<String, Object> currentContext = collectSuiteContext(currentSuite, runs);

            boolean allFinished = true;
            boolean anyFailed = false;
            boolean anyProgress = false;

            // Check if any run is currently active (for sequential mode)
            boolean activeRunExists = runs.stream()
                    .anyMatch(r -> "QUEUED".equals(r.getStatus()) || "IN_PROGRESS".equals(r.getStatus()));

            for (TestRun run : runs) {
                String runStatus = run.getStatus();

                if (TERMINAL_STATUSES.contains(runStatus)) {
                    if (FAILED_STATUSES.contains(runStatus)) {
                        anyFailed = true;
                    }
                } else {
                    allFinished = false;
                    if ("PENDING".equals(runStatus)) {
                        DependencyStatus depStatus = checkDependencies(run, runsByKey);

                        if (depStatus == DependencyStatus.READY) {
                            // In sequential mode, we only queue if nothing else is running
                            if (isSequential && activeRunExists) {
                                continue;
                            }

                            log.info("Dependencies satisfied for TestRun {} (Scenario: {}). Queuing.",
                                    run.getId(), run.getScenario().getKey());
                            run.setStatus("QUEUED");

                            // Inject Suite Context into TestRun so it can be used by steps
                            run.setExecutionContext(Map.of("suite", currentContext));

                            testRunRepository.save(run);
                            runsToQueue.add(run.getId().toString());

                            anyProgress = true;
                            if (isSequential)
                                activeRunExists = true;
                        } else if (depStatus == DependencyStatus.IMPOSSIBLE) {
                            log.info("Dependencies impossible for TestRun {} (Scenario: {}). Skipping.",
                                    run.getId(), run.getScenario().getKey());
                            run.setStatus("SKIPPED");
                            testRunRepository.save(run);
                            anyProgress = true;
                        }
                    }
                }
            }

            if (allFinished) {
                String finalStatus = anyFailed ? "FAILED" : "PASSED";
                log.info("SuiteRun {} finished with status {}", currentSuite.getId(), finalStatus);
                currentSuite.setStatus(finalStatus);
                currentSuite.setFinishedAt(OffsetDateTime.now());
                currentSuite.setContext(currentContext);
                suiteRunRepository.save(currentSuite);
            } else if (!currentContext.equals(currentSuite.getContext())) {
                currentSuite.setContext(currentContext);
                suiteRunRepository.save(currentSuite);
            } else if (anyProgress) {
                log.debug("SuiteRun {} made progress", currentSuite.getId());
            }
        });

        // Send messages after transaction commit to avoid race conditions
        for (String runId : runsToQueue) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RUN_JOBS_EXCHANGE,
                    RabbitMQConfig.RUN_JOBS_ROUTING_KEY,
                    runId);
        }
    }

    private DependencyStatus checkDependencies(TestRun run, Map<String, TestRun> runsByKey) {
        List<ScenarioDependency> dependencies = run.getScenario().getDependsOn();

        if (dependencies == null || dependencies.isEmpty()) {
            return DependencyStatus.READY; // No dependencies, ready to run
        }

        for (ScenarioDependency dep : dependencies) {
            String requiredKey = dep.getScenarioKey();
            List<String> requiredStatuses = dep.getOnStatus();

            TestRun dependencyRun = runsByKey.get(requiredKey);
            if (dependencyRun == null) {
                log.warn("Dependency scenario {} not found in suite run {}", requiredKey, run.getSuiteRun().getId());
                return DependencyStatus.IMPOSSIBLE; // Dependency missing in this run
            }

            String status = dependencyRun.getStatus();

            if (requiredStatuses.contains(status)) {
                continue; // This dependency is satisfied
            }

            if (TERMINAL_STATUSES.contains(status)) {
                // Dependency finished but with wrong status -> Impossible to satisfy
                return DependencyStatus.IMPOSSIBLE;
            }

            // Dependency is still running or pending -> Wait
            return DependencyStatus.WAITING;
        }
        return DependencyStatus.READY;
    }

    private Map<String, Object> collectSuiteContext(SuiteRun suite, List<TestRun> runs) {
        Map<String, Object> context = new HashMap<>();
        if (suite.getContext() != null) {
            context.putAll(suite.getContext());
        }

        for (TestRun run : runs) {
            if ("PASSED".equals(run.getStatus())) {
                extractExports(run, context);
            }
        }
        return context;
    }

    private void extractExports(TestRun run, Map<String, Object> context) {
        List<ScenarioStep> steps = run.getScenario().getSteps();
        // Optimization: only fetch results if there are exports defined
        boolean hasExports = steps.stream().anyMatch(s -> {
            return s.getExportAs() != null && !s.getExportAs().isEmpty();
        });

        if (!hasExports)
            return;

        List<TestStepResult> results = testStepResultRepository.findByRunIdOrderByStartedAtAsc(run.getId());
        Map<String, TestStepResult> resultsByAlias = results.stream()
                .collect(Collectors.toMap(TestStepResult::getStepAlias, r -> r, (r1, r2) -> r2));

        for (ScenarioStep step : steps) {
            Map<String, String> exports = step.getExportAs();
            if (exports == null || exports.isEmpty())
                continue;

            TestStepResult result = resultsByAlias.get(step.getAlias());

            if (result != null && "PASSED".equals(result.getStatus()) && result.getPayload() != null) {
                for (Map.Entry<String, String> entry : exports.entrySet()) {
                    String varName = entry.getKey();
                    String path = entry.getValue();
                    Object value = extractValue(result.getPayload(), path);
                    if (value != null) {
                        context.put(varName, value);
                    }
                }
            }
        }
    }

    private Object extractValue(Map<String, Object> payload, String path) {
        String[] parts = path.split("\\.");
        Object current = payload;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private enum DependencyStatus {
        READY, WAITING, IMPOSSIBLE
    }
}
