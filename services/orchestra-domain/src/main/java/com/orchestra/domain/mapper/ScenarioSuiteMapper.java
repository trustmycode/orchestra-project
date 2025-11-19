package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.ScenarioSuiteDetail;
import com.orchestra.domain.dto.ScenarioSuiteSummary;
import com.orchestra.domain.model.ScenarioSuite;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.repository.TestScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScenarioSuiteMapper {

    private final TestScenarioRepository testScenarioRepository;
    private final TestScenarioMapper testScenarioMapper;

    public ScenarioSuiteSummary toSummary(ScenarioSuite entity) {
        ScenarioSuiteSummary dto = new ScenarioSuiteSummary();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getProcess() != null) {
            dto.setProcessId(entity.getProcess().getId());
        }
        if (entity.getProcessVersion() != null) {
            dto.setProcessVersion(entity.getProcessVersion().getVersion());
        }
        dto.setTags(entity.getTags());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public ScenarioSuiteDetail toDetail(ScenarioSuite entity) {
        ScenarioSuiteDetail dto = new ScenarioSuiteDetail();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getProcess() != null) {
            dto.setProcessId(entity.getProcess().getId());
        }
        if (entity.getProcessVersion() != null) {
            dto.setProcessVersion(entity.getProcessVersion().getVersion());
        }
        dto.setTags(entity.getTags());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDescription(entity.getDescription());

        List<TestScenario> scenarios = testScenarioRepository.findBySuiteIdWithSuite(entity.getId());
        dto.setScenarios(scenarios.stream()
                .filter(TestScenario::isActive)
                .map(testScenarioMapper::toSummary)
                .collect(Collectors.toList()));

        return dto;
    }
}
