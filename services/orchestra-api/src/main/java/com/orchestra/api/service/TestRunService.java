package com.orchestra.api.service;

import com.orchestra.api.config.RabbitMQConfig;
import com.orchestra.domain.dto.TestRunCreateRequest;
import com.orchestra.domain.dto.TestRunDetail;
import com.orchestra.domain.dto.TestRunSummary;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.mapper.TestRunMapper;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.TestDataSet;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.model.TestStepResult;
import com.orchestra.domain.repository.EnvironmentRepository;
import com.orchestra.domain.repository.TestDataSetRepository;
import com.orchestra.domain.repository.TestRunRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import com.orchestra.domain.repository.TestStepResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunService {

    private final TestRunRepository testRunRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestStepResultRepository testStepResultRepository;
    private final TestDataSetRepository testDataSetRepository;
    private final EnvironmentRepository environmentRepository;
    private final TestRunMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public List<TestRunSummary> findAll() {
        return testRunRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    public TestRunDetail findRunById(UUID runId) {
        Objects.requireNonNull(runId, "runId is required");
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("TestRun not found with id: " + runId));
        List<TestStepResult> results = testStepResultRepository.findByRunIdOrderByStartedAtAsc(runId);
        return mapper.toDetail(run, results);
    }

    @Transactional
    public TestRunDetail createAndRunTest(TestRunCreateRequest request) {
        TestScenario scenario = testScenarioRepository.findByIdWithDetails(request.getScenarioId())
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario not found with id: " + request.getScenarioId()));

        TestDataSet dataSet = null;
        if (request.getDataSetId() != null) {
            dataSet = testDataSetRepository.findById(request.getDataSetId())
                    .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + request.getDataSetId()));
        }

        Environment environment = null;
        if (request.getEnvironmentId() != null) {
            environment = environmentRepository.findById(request.getEnvironmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Environment not found with id: " + request.getEnvironmentId()));
        }

        TestRun run = new TestRun();
        run.setId(UUID.randomUUID());
        run.setScenario(scenario);
        run.setScenarioVersion(scenario.getVersion());
        run.setMode("RUN_ALL_STEPS");
        run.setStatus("QUEUED");
        run.setTenant(scenario.getTenant());
        run.setDataSet(dataSet);
        run.setEnvironment(environment);
        testRunRepository.save(run);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RUN_JOBS_EXCHANGE,
                RabbitMQConfig.RUN_JOBS_ROUTING_KEY,
                run.getId().toString()
        );
        log.info("TestRun {} queued for execution", run.getId());

        return mapper.toDetail(run, Collections.emptyList());
    }
}
