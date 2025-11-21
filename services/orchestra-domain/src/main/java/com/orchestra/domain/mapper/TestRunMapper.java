package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.StepResultDto;
import com.orchestra.domain.dto.TestRunDetail;
import com.orchestra.domain.dto.TestRunSummary;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.model.TestStepResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TestRunMapper {

    public TestRunSummary toSummary(TestRun entity) {
        TestRunSummary dto = new TestRunSummary();
        dto.setId(entity.getId());
        dto.setScenarioId(entity.getScenario().getId());
        dto.setScenarioName(entity.getScenario().getName());
        dto.setScenarioVersion(entity.getScenarioVersion());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        if (entity.getSuiteRun() != null) {
            dto.setSuiteRunId(entity.getSuiteRun().getId());
        }
        if (entity.getEnvironment() != null) {
            dto.setEnvironmentId(entity.getEnvironment().getId());
            dto.setEnvironmentName(entity.getEnvironment().getName());
        }
        if (entity.getDataSet() != null) {
            dto.setDataSetId(entity.getDataSet().getId());
            dto.setDataSetName(entity.getDataSet().getName());
        }
        return dto;
    }

    public TestRunDetail toDetail(TestRun entity, List<TestStepResult> results) {
        TestRunDetail dto = new TestRunDetail();
        dto.setId(entity.getId());
        dto.setScenarioId(entity.getScenario().getId());
        dto.setScenarioName(entity.getScenario().getName());
        dto.setScenarioVersion(entity.getScenarioVersion());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        if (entity.getSuiteRun() != null) {
            dto.setSuiteRunId(entity.getSuiteRun().getId());
        }
        if (entity.getEnvironment() != null) {
            dto.setEnvironmentId(entity.getEnvironment().getId());
            dto.setEnvironmentName(entity.getEnvironment().getName());
        }
        if (entity.getDataSet() != null) {
            dto.setDataSetId(entity.getDataSet().getId());
            dto.setDataSetName(entity.getDataSet().getName());
        }
        dto.setExecutionContext(entity.getExecutionContext());
        dto.setStepResults(results.stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    @SuppressWarnings("unchecked")
    private StepResultDto toDto(TestStepResult entity) {
        StepResultDto dto = new StepResultDto();
        dto.setStepId(entity.getStepId());
        dto.setStepAlias(entity.getStepAlias());
        dto.setStatus(entity.getStatus());
        dto.setDurationMs(entity.getDurationMs());
        dto.setPayload(entity.getPayload());
        dto.setInputContextSnapshot(entity.getInputContextSnapshot());
        dto.setOutputContextDelta(entity.getOutputContextDelta());
        if (entity.getViolations() != null && entity.getViolations().get("violations") instanceof List) {
            dto.setViolations((List<Map<String, Object>>) entity.getViolations().get("violations"));
        }
        return dto;
    }
}
