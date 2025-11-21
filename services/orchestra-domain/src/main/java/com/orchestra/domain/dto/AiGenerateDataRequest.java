package com.orchestra.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AiGenerateDataRequest {
    private UUID scenarioId;
    private UUID stepId;
    private String mode; // HAPPY_PATH, NEGATIVE, BOUNDARY
    private UUID environmentId;
}

