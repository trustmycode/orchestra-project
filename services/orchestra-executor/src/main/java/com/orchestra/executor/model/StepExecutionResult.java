package com.orchestra.executor.model;

import java.util.Map;

public record StepExecutionResult(
    Map<String, Object> structuredOutput,
    Map<String, Object> payloadForContext
) {
    public static StepExecutionResult empty() {
        return new StepExecutionResult(Map.of(), Map.of());
    }
}


