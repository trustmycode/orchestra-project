package com.orchestra.api.service;

import com.orchestra.api.dto.TestScenarioDetail;
import com.orchestra.api.dto.TestScenarioSummary;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.api.mapper.TestScenarioMapper;
import com.orchestra.api.model.ScenarioStep;
import com.orchestra.api.model.ScenarioSuite;
import com.orchestra.api.model.Tenant;
import com.orchestra.api.model.TestScenario;
import com.orchestra.api.repository.ScenarioSuiteRepository;
import com.orchestra.api.repository.TenantRepository;
import com.orchestra.api.repository.TestScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestScenarioService {

    private final TestScenarioRepository testScenarioRepository;
    private final TenantRepository tenantRepository;
    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final TestScenarioMapper mapper;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional(readOnly = true)
    public List<TestScenarioSummary> findAll() {
        return testScenarioRepository.findAll().stream()
                .filter(TestScenario::isActive)
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestScenarioDetail findById(UUID id) {
        TestScenario scenario = testScenarioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario not found with id: " + id));
        return mapper.toDetail(scenario);
    }

    @Transactional
    public TestScenarioDetail create(TestScenarioDetail dto) {
        TestScenario scenario = mapper.toEntity(dto);
        scenario.setId(UUID.randomUUID());

        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));
        scenario.setTenant(tenant);

        if (dto.getSuiteId() != null) {
            ScenarioSuite suite = scenarioSuiteRepository.findById(dto.getSuiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("ScenarioSuite not found with id: " + dto.getSuiteId()));
            scenario.setSuite(suite);
        }

        TestScenario savedScenario = testScenarioRepository.save(scenario);
        return mapper.toDetail(savedScenario);
    }

    @Transactional
    public TestScenarioDetail update(UUID id, TestScenarioDetail dto) {
        TestScenario oldScenario = testScenarioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario not found with id: " + id));

        oldScenario.setStatus("DEPRECATED");
        oldScenario.setActive(false);
        testScenarioRepository.save(oldScenario);

        TestScenario newScenario = new TestScenario();
        newScenario.setId(UUID.randomUUID());
        newScenario.setTenant(oldScenario.getTenant());
        newScenario.setSuite(oldScenario.getSuite());
        newScenario.setKey(oldScenario.getKey());
        newScenario.setVersion(oldScenario.getVersion() + 1);
        newScenario.setStatus("DRAFT");
        newScenario.setActive(true);

        newScenario.setName(dto.getName());
        newScenario.setTags(dto.getTags());

        if (dto.getSteps() != null) {
            dto.getSteps().forEach(stepDto -> {
                ScenarioStep step = mapper.toEntity(stepDto);
                step.setScenario(newScenario);
                newScenario.getSteps().add(step);
            });
        }

        if (dto.getSuiteId() != null && (newScenario.getSuite() == null || !newScenario.getSuite().getId().equals(dto.getSuiteId()))) {
            ScenarioSuite suite = scenarioSuiteRepository.findById(dto.getSuiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("ScenarioSuite not found with id: " + dto.getSuiteId()));
            newScenario.setSuite(suite);
        } else if (dto.getSuiteId() == null) {
            newScenario.setSuite(null);
        }

        TestScenario savedScenario = testScenarioRepository.save(newScenario);
        return mapper.toDetail(savedScenario);
    }
}
