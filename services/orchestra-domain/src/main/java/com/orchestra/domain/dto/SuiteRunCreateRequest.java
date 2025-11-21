package com.orchestra.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SuiteRunCreateRequest {
    private UUID suiteId;
    private UUID environmentId;
    private String runMode;
}

