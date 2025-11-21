package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SuiteRunSummary {
    private UUID id;
    private UUID suiteId;
    private String suiteName;
    private String status;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
}

