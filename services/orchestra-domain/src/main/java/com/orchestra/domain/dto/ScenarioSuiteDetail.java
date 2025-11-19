package com.orchestra.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioSuiteDetail extends ScenarioSuiteSummary {
    private String description;
    private List<TestScenarioSummary> scenarios;
}
