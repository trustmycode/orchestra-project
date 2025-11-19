package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ScenarioSuiteSummary {
    private UUID id;
    private String name;
    private UUID processId;
    private Integer processVersion;
    private List<String> tags;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
