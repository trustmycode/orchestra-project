package com.orchestra.api.mapper;

import com.orchestra.api.dto.StepResultDto;
import com.orchestra.api.dto.TestRunDetail;
import com.orchestra.api.dto.TestRunSummary;
import com.orchestra.api.model.TestRun;
import com.orchestra.api.model.TestStepResult;
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
        dto.setScenarioVersion(entity.getScenarioVersion());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        return dto;
    }

    public TestRunDetail toDetail(TestRun entity, List<TestStepResult> results) {
        TestRunDetail dto = new TestRunDetail();
        dto.setId(entity.getId());
        dto.setScenarioId(entity.getScenario().getId());
        dto.setScenarioVersion(entity.getScenarioVersion());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
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
        if (entity.getViolations() != null && entity.getViolations().get("violations") instanceof List) {
            dto.setViolations((List<Map<String, Object>>) entity.getViolations().get("violations"));
        }
        return dto;
    }
}
