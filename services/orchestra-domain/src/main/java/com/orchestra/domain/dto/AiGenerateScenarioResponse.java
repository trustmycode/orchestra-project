package com.orchestra.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGenerateScenarioResponse {
    private Map<String, Object> globalContext;
    // Map<StepAlias, DataObject>
    private Map<String, Object> stepData;
}

