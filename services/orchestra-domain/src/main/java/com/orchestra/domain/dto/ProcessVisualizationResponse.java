package com.orchestra.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessVisualizationResponse {
    private String processId;
    private String format;
    private String sourceUrl;
}
