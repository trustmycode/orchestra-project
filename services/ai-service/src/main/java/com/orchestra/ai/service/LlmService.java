package com.orchestra.ai.service;

import com.orchestra.ai.agent.DataPlannerAgent;
import com.orchestra.ai.model.DataPlan;
import com.orchestra.domain.dto.AiReasoningLevel;
import com.orchestra.ai.model.GenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private static final int MAX_RETRIES = 3;

    private final DataPlannerAgent dataPlannerAgent;

    /**
     * Внешний API: обёртка над типизированным вызовом.
     * Возвращает Map для совместимости с текущими контроллерами.
     */
    public Map<String, Object> generate(Map<String, Object> context, AiReasoningLevel level) {
        GenerationResult result = generateTyped(context, level);
        return Map.of(
                "result", result.data(),
                "notes", result.notes() != null ? result.notes() : "");
    }

    /**
     * Типизированный вызов LLM с гарантированным structured output.
     */
    public GenerationResult generateTyped(Map<String, Object> context, AiReasoningLevel level) {
        Exception lastException = null;
        String lastError = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Delegate to DataPlannerAgent
                DataPlan plan = dataPlannerAgent.execute(context, level, lastError);

                // Map DataPlan to GenerationResult
                return new GenerationResult(plan.criteria(), plan.reasoning());
            } catch (Exception e) {
                lastException = e;
                lastError = e.getMessage();
                log.warn("Attempt {} failed to generate data plan", attempt, e);
            }
        }

        log.error("Failed to generate DataPlan after {} attempts", MAX_RETRIES, lastException);
        throw new RuntimeException("Failed to generate DataPlan", lastException);
    }

}
