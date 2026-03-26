package com.orchestra.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ScenarioSuiteGenerateRequest {
    private UUID processId;
    private Integer processVersion;
    private String name;
    private String generationMode;
    private Integer maxScenarios;
    private Integer maxLoopIterations;
    private Boolean includeParallelCombinations;
    private List<String> tags;
    private Map<String, String> specBindings;
    private UUID environmentId;
}

