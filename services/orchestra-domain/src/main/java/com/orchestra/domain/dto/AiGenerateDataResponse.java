package com.orchestra.domain.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AiGenerateDataResponse {
    private Map<String, Object> data;
    private String notes;
}

