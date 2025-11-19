package com.orchestra.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestRunDetail extends TestRunSummary {
    private List<StepResultDto> stepResults;
    private Map<String, Object> executionContext;
}
