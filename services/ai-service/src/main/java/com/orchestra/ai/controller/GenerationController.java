package com.orchestra.ai.controller;

import com.orchestra.ai.context.AiContext;
import com.orchestra.ai.service.LlmService;
import com.orchestra.domain.dto.AiReasoningLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class GenerationController {

    private final LlmService llmService;

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> request) {
        String levelStr = (String) request.getOrDefault("reasoning", "MEDIUM");
        String tenantIdStr = (String) request.get("tenantId");

        AiReasoningLevel level;
        try {
            level = AiReasoningLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            level = AiReasoningLevel.MEDIUM;
        }

        if (tenantIdStr != null) {
            AiContext.setTenantId(UUID.fromString(tenantIdStr));
        }

        try {
            return llmService.generate(request, level);
        } finally {
            AiContext.clear();
        }
    }
}