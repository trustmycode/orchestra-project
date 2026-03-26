package com.orchestra.domain.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AiDataTransferResponse {
    private Map<String, String> mapping;
    private String reasoning;
}


