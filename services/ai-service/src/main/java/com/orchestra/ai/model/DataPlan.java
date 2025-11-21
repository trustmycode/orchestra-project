package com.orchestra.ai.model;

import java.util.Map;

public record DataPlan(
    Map<String, Object> criteria,
    String strategy,
    String reasoning
) {}

