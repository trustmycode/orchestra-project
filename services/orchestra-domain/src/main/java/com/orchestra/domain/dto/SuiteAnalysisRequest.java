package com.orchestra.domain.dto;

import java.util.List;

public record SuiteAnalysisRequest(
    List<ScenarioSummary> scenarios,
    String instructions
) {
    public record ScenarioSummary(
        String scenarioName,
        List<Variable> variables
    ) {}

    public record Variable(
        String name,
        String description,
        String type
    ) {}
}

