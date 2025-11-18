package com.orchestra.api.service;

import com.orchestra.api.dto.ScenarioSuiteCreateRequest;
import com.orchestra.api.dto.ScenarioSuiteDetail;
import com.orchestra.api.dto.ScenarioSuiteSummary;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.api.mapper.ScenarioSuiteMapper;
import com.orchestra.api.model.Process;
import com.orchestra.api.model.ScenarioSuite;
import com.orchestra.api.model.Tenant;
import com.orchestra.api.repository.ProcessRepository;
import com.orchestra.api.repository.ScenarioSuiteRepository;
import com.orchestra.api.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScenarioSuiteService {

    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final ProcessRepository processRepository;
    private final TenantRepository tenantRepository;
    private final ScenarioSuiteMapper mapper;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional(readOnly = true)
    public List<ScenarioSuiteSummary> findAll() {
        return scenarioSuiteRepository.findAll().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScenarioSuiteDetail findById(UUID id) {
        return scenarioSuiteRepository.findById(id)
                .map(mapper::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("ScenarioSuite not found with id: " + id));
    }

    @Transactional
    public ScenarioSuiteDetail create(ScenarioSuiteCreateRequest dto) {
        Process process = processRepository.findById(dto.getProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("Process not found with id: " + dto.getProcessId()));

        // For now, we don't link to a specific process version, but this can be extended later
        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));

        ScenarioSuite suite = new ScenarioSuite();
        suite.setId(UUID.randomUUID());
        suite.setTenant(tenant);
        suite.setProcess(process);
        suite.setName(dto.getName());
        suite.setDescription(dto.getDescription());
        suite.setTags(dto.getTags());

        ScenarioSuite saved = scenarioSuiteRepository.save(suite);
        return mapper.toDetail(saved);
    }
}
