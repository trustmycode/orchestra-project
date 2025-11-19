package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TestDataSetSummary {
    private UUID id;
    private String scope;
    private UUID suiteId;
    private UUID scenarioId;
    private String name;
    private List<String> tags;
    private String origin;
    private OffsetDateTime createdAt;
    private String createdBy;
}
