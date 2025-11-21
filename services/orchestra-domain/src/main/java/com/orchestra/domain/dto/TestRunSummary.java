package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TestRunSummary {
    private UUID id;
    private UUID scenarioId;
    private String scenarioName;
    private Integer scenarioVersion;
    private String status;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private UUID suiteRunId;
    private UUID environmentId;
    private String environmentName;
    private UUID dataSetId;
    private String dataSetName;
}
