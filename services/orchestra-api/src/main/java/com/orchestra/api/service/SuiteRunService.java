package com.orchestra.api.service;

import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.dto.SuiteRunCreateRequest;
import com.orchestra.domain.dto.SuiteRunDetail;
import com.orchestra.domain.dto.SuiteRunSummary;
import com.orchestra.domain.mapper.SuiteRunMapper;
import com.orchestra.domain.model.*;
import com.orchestra.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuiteRunService {

    private final SuiteRunRepository suiteRunRepository;
    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestRunRepository testRunRepository;
    private final EnvironmentRepository environmentRepository;
    private final TenantRepository tenantRepository;
    private final SuiteRunMapper suiteRunMapper;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional
    public SuiteRunSummary createAndSchedule(SuiteRunCreateRequest request) {
        log.info("Creating SuiteRun for suiteId: {}", request.getSuiteId());

        ScenarioSuite suite = scenarioSuiteRepository.findById(request.getSuiteId())
                .orElseThrow(() -> new ResourceNotFoundException("ScenarioSuite not found: " + request.getSuiteId()));

        Environment environment = null;
        if (request.getEnvironmentId() != null) {
            environment = environmentRepository.findById(request.getEnvironmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + request.getEnvironmentId()));
        }

        // 1. Create SuiteRun
        SuiteRun suiteRun = new SuiteRun();
        suiteRun.setId(UUID.randomUUID());
        suiteRun.setTenant(suite.getTenant());
        suiteRun.setSuite(suite);
        suiteRun.setStatus("PENDING");
        suiteRun.setRunMode(request.getRunMode() != null ? request.getRunMode() : "PARALLEL");
        suiteRun.setContext(new HashMap<>()); // Empty context initially
        
        // Save SuiteRun first
        suiteRun = suiteRunRepository.save(suiteRun);

        // 2. Find all active scenarios in the suite
        List<TestScenario> scenarios = testScenarioRepository.findBySuiteIdWithSuite(suite.getId());
        
        if (scenarios.isEmpty()) {
            log.warn("No active scenarios found for suite: {}", suite.getId());
            // Optionally mark suite run as finished/failed immediately, but for now leave as PENDING for scheduler to handle or fail
        }

        // 3. Create TestRuns for each scenario
        for (TestScenario scenario : scenarios) {
            if (!scenario.isActive()) continue;

            TestRun testRun = new TestRun();
            testRun.setId(UUID.randomUUID());
            testRun.setTenant(scenario.getTenant());
            testRun.setScenario(scenario);
            testRun.setScenarioVersion(scenario.getVersion());
            testRun.setSuiteRun(suiteRun);
            testRun.setEnvironment(environment);
            
            // Default mode
            testRun.setMode("RUN_ALL_STEPS");
            
            // Initial status is PENDING. The Scheduler (SuiteRun Orchestrator) will pick these up
            // based on dependencies and move them to QUEUED.
            testRun.setStatus("PENDING");
            
            // Note: DataSet resolution from tag is skipped for MVP, can be added here
            
            testRunRepository.save(testRun);
        }

        log.info("Created SuiteRun {} with {} test runs", suiteRun.getId(), scenarios.size());
        
        return suiteRunMapper.toSummary(suiteRun);
    }

    @Transactional(readOnly = true)
    public List<SuiteRunSummary> findAll() {
        return suiteRunRepository.findAll().stream()
                .map(suiteRunMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuiteRunDetail findById(UUID id) {
        SuiteRun suiteRun = suiteRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SuiteRun not found: " + id));
        
        // Fetch child test runs
        // Note: This might be heavy if there are many runs, pagination should be considered later
        // For now, we fetch them via repository to avoid N+1 if lazy loading isn't initialized
        // But since we need them for the detail view, we can rely on a custom query or lazy loading if session is open.
        // Let's assume lazy loading works within transaction or use a repository method.
        // Since we are in @Transactional, lazy loading of testRuns list (if we added OneToMany in SuiteRun) would work.
        // However, SuiteRun entity (created in this task) might not have the list mapped yet or we fetch via TestRunRepository.
        
        return suiteRunMapper.toDetail(suiteRun, testRunRepository.findBySuiteRunId(id));
    }
}

