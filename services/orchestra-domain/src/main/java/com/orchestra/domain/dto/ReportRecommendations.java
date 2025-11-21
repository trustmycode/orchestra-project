package com.orchestra.domain.dto;

import java.util.List;

public record ReportRecommendations(
    List<String> scenarioImprovements,
    List<String> dataImprovements,
    List<String> specImprovements
) {}

