package com.orchestra.domain.dto;

import java.util.List;
import java.util.Map;

public record ScenarioAnalysisRequest(
    String scenarioName,
    String scenarioDescription,
    List<StepMetadata> steps
) {
    public record StepMetadata(
        String alias,
        String name,
        String type,
        Map<String, Object> action
    ) {}
}

