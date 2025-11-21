package com.orchestra.domain.dto;

import java.util.List;
import java.util.Map;

public record ReportAnalysisRequest(
    String scenarioName,
    String status,
    List<FailedStepDetail> failedSteps
) {
    public record FailedStepDetail(
        String stepAlias,
        String errorMessage,
        Map<String, Object> lastPayload
    ) {}
}

