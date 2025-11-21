package com.orchestra.executor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.model.TestStepResult;
import com.orchestra.domain.repository.SuiteRunRepository;
import com.orchestra.domain.repository.TestRunRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import com.orchestra.domain.repository.TestStepResultRepository;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import com.orchestra.executor.plugin.ProtocolPluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunExecutorService {

    private final TestRunRepository testRunRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestStepResultRepository testStepResultRepository;
    private final SuiteRunRepository suiteRunRepository;
    private final ProtocolPluginRegistry protocolPluginRegistry;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    private final String workerId = UUID.randomUUID().toString();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public void execute(UUID testRunId) {
        final UUID resolvedTestRunId = Objects.requireNonNull(testRunId, "testRunId must not be null");
        log.info("Processing job for TestRun: {}", resolvedTestRunId);

        TestRun run = transactionTemplate.execute(status -> {
            TestRun r = testRunRepository.findById(resolvedTestRunId)
                    .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + resolvedTestRunId));
            if (r.getSuiteRun() != null) {
                r.getSuiteRun().getId();
            }
            return r;
        });

        if (run == null) {
            return;
        }

        if (Set.of("PASSED", "FAILED", "CANCELLED", "FAILED_STUCK").contains(run.getStatus())) {
            log.info("TestRun {} is already in terminal state: {}. Skipping execution.", resolvedTestRunId, run.getStatus());
            return;
        }

        if ("IN_PROGRESS".equals(run.getStatus()) && run.getLockUntil() != null && run.getLockUntil().isAfter(OffsetDateTime.now())) {
            log.info("TestRun {} is already running (locked by {}). Skipping execution.", resolvedTestRunId, run.getLockedBy());
            return;
        }

        log.info("Attempting to acquire lock for TestRun: {} with workerId: {}", resolvedTestRunId, workerId);
        if (!tryAcquireLock(resolvedTestRunId)) {
            log.warn("Could not acquire lock for TestRun: {}. It might be running on another worker.", resolvedTestRunId);
            return;
        }

        ScheduledFuture<?> heartbeatTask = startHeartbeat(resolvedTestRunId);

        try {
            // 1. Load Scenario
            TestScenario scenario = testScenarioRepository.findByIdWithDetails(run.getScenario().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Scenario not found"));

            // 2. Restore Context
            ExecutionContext context = new ExecutionContext();
            if (run.getExecutionContext() != null) {
                context.setVariables(new HashMap<>(run.getExecutionContext()));
            }

            // 2.1 Load Suite Context if part of a suite run
            if (run.getSuiteRun() != null) {
                Map<String, Object> suiteContext = suiteRunRepository.findContextById(run.getSuiteRun().getId());
                if (suiteContext != null) {
                    suiteContext.forEach((k, v) -> context.getVariables().put("suite." + k, v));
                }
            }

            // 3. Determine steps to run (Resume capability)
            List<TestStepResult> existingResults = testStepResultRepository.findByRunIdOrderByStartedAtAsc(resolvedTestRunId);
            Set<UUID> executedStepIds = existingResults.stream()
                    .map(TestStepResult::getStepId)
                    .collect(Collectors.toSet());

            // 4. Execute Steps
            for (ScenarioStep step : scenario.getSteps()) {
                if (executedStepIds.contains(step.getId())) {
                    continue;
                }
                executeStep(run, step, context);
            }

            // 5. Complete Run
            transactionTemplate.executeWithoutResult(status -> {
                TestRun r = testRunRepository.findById(resolvedTestRunId).orElseThrow();
                r.setStatus("PASSED");
                r.setFinishedAt(OffsetDateTime.now());
                testRunRepository.save(r);
            });
        } catch (Exception e) {
            log.error("TestRun execution failed for id: {}", resolvedTestRunId, e);
            transactionTemplate.executeWithoutResult(status -> {
                TestRun r = testRunRepository.findById(resolvedTestRunId).orElseThrow();
                r.setStatus("FAILED");
                r.setFinishedAt(OffsetDateTime.now());
                testRunRepository.save(r);
            });
        } finally {
            if (heartbeatTask != null) {
                heartbeatTask.cancel(true);
            }
        }
    }

    public boolean tryAcquireLock(UUID testRunId) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime lockUntil = now.plusSeconds(60);
            int rows = testRunRepository.acquireLock(testRunId, workerId, lockUntil, now);
            return rows > 0;
        }));
    }

    private ScheduledFuture<?> startHeartbeat(UUID testRunId) {
        return heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat(testRunId);
            } catch (Exception e) {
                log.error("Failed to send heartbeat for run {}", testRunId, e);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public void sendHeartbeat(UUID testRunId) {
        transactionTemplate.executeWithoutResult(status -> {
            int updated = testRunRepository.updateHeartbeat(
                    testRunId,
                    workerId,
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusSeconds(60));

            if (updated == 0) {
                log.warn("Heartbeat update skipped for TestRun: {}. Lock might be lost.", testRunId);
            }
        });
    }

    private void executeStep(TestRun run, ScenarioStep step, ExecutionContext context) {
        Map<String, Object> inputSnapshot = new HashMap<>(context.getVariables());

        ProtocolPlugin plugin = protocolPluginRegistry.getPlugin(step.getChannelType())
                .orElseThrow(() -> new RuntimeException("No plugin for " + step.getChannelType()));

        OffsetDateTime start = OffsetDateTime.now();
        try {
            plugin.execute(step, context, run);
        } catch (Exception e) {
            OffsetDateTime finish = OffsetDateTime.now();
            long duration = Duration.between(start, finish).toMillis();
            log.error("Step {} failed", step.getAlias(), e);

            transactionTemplate.executeWithoutResult(status -> {
                TestStepResult result = new TestStepResult();
                result.setRun(run);
                result.setStepId(step.getId());
                result.setStepAlias(step.getAlias());
                result.setStatus("FAILED");
                result.setStartedAt(start);
                result.setFinishedAt(finish);
                result.setDurationMs(duration);
                result.setInputContextSnapshot(inputSnapshot);
                result.setViolations(Map.of("violations", List.of(Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"))));
                testStepResultRepository.save(result);
            });
            throw e;
        }

        OffsetDateTime finish = OffsetDateTime.now();

        Map<String, Object> delta = new HashMap<>();
        context.getVariables().forEach((k, v) -> {
            if (!Objects.equals(v, inputSnapshot.get(k))) {
                delta.put(k, v);
            }
        });

        Map<String, Object> exports = new HashMap<>();
        if (step.getExportAs() != null && !step.getExportAs().isEmpty()) {
            step.getExportAs().forEach((varName, path) -> {
                Object value = extractValue(context.getVariables(), path);
                if (value != null) {
                    exports.put(varName, value);
                }
            });
        }

        transactionTemplate.executeWithoutResult(status -> {
            TestStepResult result = new TestStepResult();
            result.setRun(run);
            result.setStepId(step.getId());
            result.setStepAlias(step.getAlias());
            result.setStatus("PASSED");
            result.setStartedAt(start);
            result.setFinishedAt(finish);
            result.setDurationMs(Duration.between(start, finish).toMillis());
            result.setInputContextSnapshot(inputSnapshot);
            result.setOutputContextDelta(delta);
            testStepResultRepository.save(result);

            if (!exports.isEmpty() && run.getSuiteRun() != null) {
                try {
                    String json = objectMapper.writeValueAsString(exports);
                    suiteRunRepository.updateContext(run.getSuiteRun().getId(), json);
                } catch (Exception e) {
                    log.error("Failed to update suite context for run {}", run.getId(), e);
                }
            }

            UUID runId = Objects.requireNonNull(run.getId(), "run id must not be null");
            TestRun currentRun = testRunRepository.findById(runId).orElseThrow();
            currentRun.setExecutionContext(new HashMap<>(context.getVariables()));
            testRunRepository.save(currentRun);
        });

        exports.forEach((k, v) -> context.getVariables().put("suite." + k, v));
    }

    private Object extractValue(Map<String, Object> variables, String path) {
        String[] parts = path.split("\\.");
        Object current = variables;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
