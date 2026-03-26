package com.orchestra.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class AiDataTransferRequest {
    private String targetStepName;
    private Object targetSchema;
    private List<AvailableOutput> availableOutputs;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AvailableOutput {
        private String stepAlias;
        private String stepName;
        private Object outputSchema;
    }
}


