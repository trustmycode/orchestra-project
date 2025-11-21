package com.orchestra.domain.dto;

import java.util.List;

public record ScenarioAnalysisResponse(
    List<GlobalVariable> variables
) {
    public record GlobalVariable(
        String name,
        String description,
        String type
    ) {}
}

