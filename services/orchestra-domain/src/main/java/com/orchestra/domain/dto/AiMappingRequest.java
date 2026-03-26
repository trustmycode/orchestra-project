package com.orchestra.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
public class AiMappingRequest {
    private String taskName;
    private String taskDescription;
    private UUID tenantId;
    private List<CandidateDto> candidates;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CandidateDto {
        private int index;
        private String method;
        private String path;
        private String summary;
        private String operationId;
        private String description;
    }
}

