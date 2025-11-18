package com.orchestra.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TestRunCreateRequest {
    private UUID scenarioId;
    private UUID dataSetId;
}
