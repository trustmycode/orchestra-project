package com.orchestra.ai.agent;

import com.orchestra.ai.model.DataPlan;
import com.orchestra.ai.service.PromptManagerService;
import com.orchestra.ai.service.ToolRegistry;
import com.orchestra.domain.dto.AiReasoningLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DataPlannerAgent extends BaseAgent<Map<String, Object>, DataPlan> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final PromptManagerService promptManagerService;

    public DataPlannerAgent(ChatClient.Builder builder,
            ChatMemory chatMemory,
            PromptManagerService promptManagerService,
            ToolRegistry toolRegistry) {
        super(builder, chatMemory, "You are a data planner.", toolRegistry.getGlobalTools());
        this.promptManagerService = promptManagerService;
    }

    @Override
    public DataPlan execute(Map<String, Object> context) {
        return execute(context, AiReasoningLevel.MEDIUM, null);
    }

    public DataPlan execute(Map<String, Object> context, AiReasoningLevel level) {
        return execute(context, level, null);
    }

    public DataPlan execute(Map<String, Object> context, AiReasoningLevel level, @Nullable String lastError) {
        BeanOutputConverter<DataPlan> converter = new BeanOutputConverter<>(DataPlan.class);

        // Имя промпта синхронизировано с TASK-2024-074 / ADR-0019
        String sysPromptTemplate = promptManagerService.getPrompt("data_planner_system_v1");
        String fullSystemPromptTemplate = sysPromptTemplate
                + """


                        You must return the response strictly in the following JSON format, without any comments or text outside the JSON:
                        {format}
                        """;

        PromptTemplate promptTemplate = new PromptTemplate(fullSystemPromptTemplate);
        String systemText = promptTemplate.render(Map.of("format", converter.getFormat()));

        OllamaChatOptions options = buildOptions(level);

        String conversationId = buildConversationId(context);

        log.info("DataPlannerAgent: Generating plan with reasoning level {} (conversationId={})",
                level, conversationId);

        String userMessage = "Context: " + context;
        if (lastError != null) {
            userMessage += "\n\nPrevious attempt failed with error: " + lastError + ". Please ensure valid JSON output matching the schema.";
        }

        // Используем базовый helper, чтобы не дублировать логику advisors/memory
        String rawResponse = callLlm(
                systemText,
                userMessage,
                options,
                conversationId);

        String cleanResponse = Objects.requireNonNull(cleanResponse(rawResponse));
        return converter.convert(cleanResponse);
    }

    /**
     * Простое построение conversationId для связки с ChatMemory.
     * Сейчас — по tenantId, чтобы память была изолирована по арендаторам.
     */
    private String buildConversationId(Map<String, Object> context) {
        Object tenantId = context.get("tenantId");
        String tenantPart = tenantId != null ? tenantId.toString() : "default";
        return "data-planner:" + tenantPart;
    }

    private OllamaChatOptions buildOptions(AiReasoningLevel level) {
        OllamaChatOptions.Builder builder = OllamaChatOptions.builder()
                .temperature(0.2);

        switch (level) {
            case LOW -> builder.thinkLow();
            case MEDIUM -> builder.thinkMedium();
            case HIGH -> builder.thinkHigh();
            default -> builder.thinkMedium();
        }
        return builder.build();
    }

    private String cleanResponse(String response) {
        if (response == null)
            return "";
        // Remove <think> blocks common in reasoning models
        String clean = THINK_PATTERN.matcher(response).replaceAll("").trim();

        // Extract JSON if wrapped in markdown or extra text
        int firstBrace = clean.indexOf('{');
        int lastBrace = clean.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            clean = clean.substring(firstBrace, lastBrace + 1);
        } else if (clean.startsWith("```")) {
            clean = clean.replace("```json", "").replace("```", "").trim();
        }
        return Objects.requireNonNullElse(clean, "");
    }
}
