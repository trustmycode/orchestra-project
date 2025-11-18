package com.orchestra.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ScenarioSuiteCreateRequest {
    private UUID processId;
    private Integer processVersion;
    private String name;
    private String description;
    private List<String> tags;
}
