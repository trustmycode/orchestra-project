package com.orchestra.executor.service;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.model.TestStepResult;
import com.orchestra.domain.repository.TestRunRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import com.orchestra.domain.repository.TestStepResultRepository;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import com.orchestra.executor.plugin.ProtocolPluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ProtocolPluginRegistry protocolPluginRegistry;
    private final TransactionTemplate transactionTemplate;

    private final String workerId = UUID.randomUUID().toString();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public void execute(UUID testRunId) {
        log.info("Processing job for TestRun: {}", testRunId);

        TestRun run = testRunRepository.findById(testRunId)
                .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + testRunId));

        if (Set.of("PASSED", "FAILED", "CANCELLED", "FAILED_STUCK").contains(run.getStatus())) {
            log.info("TestRun {} is already in terminal state: {}. Skipping execution.", testRunId, run.getStatus());
            return;
        }

        if ("IN_PROGRESS".equals(run.getStatus()) && run.getLockUntil() != null && run.getLockUntil().isAfter(OffsetDateTime.now())) {
            log.info("TestRun {} is already running (locked by {}). Skipping execution.", testRunId, run.getLockedBy());
            return;
        }

        log.info("Attempting to acquire lock for TestRun: {} with workerId: {}", testRunId, workerId);
        if (!tryAcquireLock(testRunId)) {
            log.warn("Could not acquire lock for TestRun: {}. It might be running on another worker.", testRunId);
            return;
        }

        ScheduledFuture<?> heartbeatTask = startHeartbeat(testRunId);

        try {
            // 1. Load Scenario
            TestScenario scenario = testScenarioRepository.findByIdWithDetails(run.getScenario().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Scenario not found"));

            // 2. Restore Context
            ExecutionContext context = new ExecutionContext();
            if (run.getExecutionContext() != null) {
                context.setVariables(new HashMap<>(run.getExecutionContext()));
            }

            // 3. Determine steps to run (Resume capability)
            List<TestStepResult> existingResults = testStepResultRepository.findByRunIdOrderByStartedAtAsc(testRunId);
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
                TestRun r = testRunRepository.findById(testRunId).orElseThrow();
                r.setStatus("PASSED");
                r.setFinishedAt(OffsetDateTime.now());
                testRunRepository.save(r);
            });
        } catch (Exception e) {
            log.error("TestRun execution failed for id: {}", testRunId, e);
            transactionTemplate.executeWithoutResult(status -> {
                TestRun r = testRunRepository.findById(testRunId).orElseThrow();
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
        plugin.execute(step, context, run);
        OffsetDateTime finish = OffsetDateTime.now();

        Map<String, Object> delta = new HashMap<>();
        context.getVariables().forEach((k, v) -> {
            if (!Objects.equals(v, inputSnapshot.get(k))) {
                delta.put(k, v);
            }
        });

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

            TestRun currentRun = testRunRepository.findById(run.getId()).orElseThrow();
            currentRun.setExecutionContext(new HashMap<>(context.getVariables()));
            testRunRepository.save(currentRun);
        });
    }
}
