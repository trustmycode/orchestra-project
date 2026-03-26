package com.orchestra.domain.dto;

import java.util.Map;
import java.util.UUID;

public record AiGenerateSuiteResponse(
    Map<String, Object> suiteContext,
    Map<UUID, AiGenerateScenarioResponse> scenarioData
) {
}

