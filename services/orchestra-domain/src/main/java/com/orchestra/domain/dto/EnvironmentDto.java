package com.orchestra.domain.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class EnvironmentDto {
    private UUID id;
    private String name;
    private String description;
    private Map<String, Object> profileMappings;
}