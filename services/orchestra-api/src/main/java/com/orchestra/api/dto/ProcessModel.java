package com.orchestra.api.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ProcessModel {
    private UUID id;
    private String name;
    private String sourceType;
    private OffsetDateTime createdAt;
}
