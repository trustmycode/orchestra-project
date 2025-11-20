package com.orchestra.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TestRunCreateRequest {
    private UUID scenarioId;
    private UUID dataSetId;
    private UUID environmentId;
}
