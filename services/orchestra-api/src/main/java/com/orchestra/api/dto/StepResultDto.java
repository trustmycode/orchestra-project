package com.orchestra.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class StepResultDto {
    private UUID stepId;
    private String stepAlias;
    private String status;
    private Long durationMs;
    private Map<String, Object> payload;
    private List<Map<String, Object>> violations;
}
