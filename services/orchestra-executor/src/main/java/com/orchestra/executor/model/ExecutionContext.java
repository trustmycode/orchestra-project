package com.orchestra.executor.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExecutionContext {
    private Map<String, Object> variables = new HashMap<>();
    private String correlationId;
}
