package com.orchestra.domain.dto;

import java.util.Map;

public record SuiteContextPlan(
    Map<String, Object> globalVariables
) {
}

