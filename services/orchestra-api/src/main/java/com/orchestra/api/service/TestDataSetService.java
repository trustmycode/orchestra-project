package com.orchestra.api.service;

import com.orchestra.domain.dto.TestDataSetDetail;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.mapper.TestDataSetMapper;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.model.ScenarioSuite;
import com.orchestra.domain.model.TestDataSet;
import com.orchestra.domain.repository.TenantRepository;
import com.orchestra.domain.repository.TestDataSetRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import com.orchestra.domain.repository.ScenarioSuiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestDataSetService {

    private final TestDataSetRepository testDataSetRepository;
    private final TenantRepository tenantRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final TestDataSetMapper mapper;
    private final DataIndexerService dataIndexerService;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional(readOnly = true)
    public List<TestDataSetDetail> findAll() {
        return testDataSetRepository.findAll().stream()
                .map(mapper::toDetail)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestDataSetDetail findById(UUID id) {
        TestDataSet dataSet = testDataSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + id));
        return mapper.toDetail(dataSet);
    }

    @Transactional
    public TestDataSetDetail create(TestDataSetDetail dto) {
        TestDataSet dataSet = mapper.toEntity(dto);
        dataSet.setId(UUID.randomUUID());

        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));
        dataSet.setTenant(tenant);

        TestDataSet saved = testDataSetRepository.save(dataSet);
        dataIndexerService.indexAsync(saved);
        return mapper.toDetail(saved);
    }

    @Transactional
    public TestDataSetDetail createPlaceholder(UUID suiteId, UUID scenarioId, UUID jobId) {
        TestDataSet dataSet = new TestDataSet();
        dataSet.setId(UUID.randomUUID());
        dataSet.setStatus("GENERATING");
        dataSet.setGenerationJobId(jobId);
        dataSet.setOrigin("AI_GENERATED");
        dataSet.setData(new HashMap<>()); // Empty data initially
        dataSet.setName("AI Generated Data Set " + LocalDateTime.now());

        Tenant tenant = null;

        if (scenarioId != null) {
            TestScenario scenario = testScenarioRepository.findById(scenarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + scenarioId));
            dataSet.setScenario(scenario);
            dataSet.setScope("SCENARIO");
            tenant = scenario.getTenant();
        } else if (suiteId != null) {
            ScenarioSuite suite = scenarioSuiteRepository.findById(suiteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Suite not found: " + suiteId));
            dataSet.setSuite(suite);
            dataSet.setScope("SUITE");
            tenant = suite.getTenant();
        }

        if (tenant == null) {
            tenant = tenantRepository.findById(DEFAULT_TENANT_ID).orElseThrow();
        }
        dataSet.setTenant(tenant);

        TestDataSet saved = testDataSetRepository.save(dataSet);
        return mapper.toDetail(saved);
    }

    @Transactional
    public TestDataSetDetail update(UUID id, TestDataSetDetail dto) {
        TestDataSet dataSet = testDataSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + id));

        mapper.updateEntityFromDto(dataSet, dto);
        TestDataSet updated = testDataSetRepository.save(dataSet);
        dataIndexerService.reindexAsync(updated);
        return mapper.toDetail(updated);
    }

    public void delete(UUID id) {
        if (!testDataSetRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestDataSet not found with id: " + id);
        }
        dataIndexerService.removeAsync(id);
        testDataSetRepository.deleteById(id);
    }
}
