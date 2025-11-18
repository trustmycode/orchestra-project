package com.orchestra.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestRunDetail extends TestRunSummary {
    private List<StepResultDto> stepResults;
}
