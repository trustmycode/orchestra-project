package com.orchestra.domain.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ScenarioStepDto {
    private UUID id;
    private Integer orderIndex;
    private String alias;
    private String name;
    private String kind;
    private String channelType;
    private Map<String, Object> endpointRef;
    private Map<String, String> exportAs;
    private Map<String, Object> action;
    private Map<String, Object> expectations;
}
