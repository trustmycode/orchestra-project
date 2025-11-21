package com.orchestra.ai.model;

import java.util.Map;

/**
 * Результат генерации данных от LLM.
 */
public record GenerationResult(Map<String, Object> data, String notes) { }

