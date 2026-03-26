package com.orchestra.domain.dto;

import lombok.Data;

@Data
public class AiMappingResponse {
    private Integer selectedCandidateIndex;
    private Double confidence;
    private String reasoning;
}

