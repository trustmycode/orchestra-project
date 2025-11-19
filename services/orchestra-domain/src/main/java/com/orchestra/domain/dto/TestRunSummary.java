package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TestRunSummary {
    private UUID id;
    private UUID scenarioId;
    private Integer scenarioVersion;
    private String status;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
}
