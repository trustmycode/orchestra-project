package com.orchestra.domain.dto;

import com.orchestra.domain.model.ScenarioDependency;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TestScenarioDetail {
    private UUID id;
    private String name;
    private UUID suiteId;
    private UUID processId;
    private Integer processVersion;
    private String key;
    private boolean isActive;
    private Integer version;
    private String status;
    private List<String> tags;
    private List<ScenarioStepDto> steps;
    private List<ScenarioDependency> dependsOn;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
