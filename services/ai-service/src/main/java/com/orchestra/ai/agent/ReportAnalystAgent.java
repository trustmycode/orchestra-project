package com.orchestra.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.ai.service.PromptManagerService;
import com.orchestra.domain.dto.ReportAnalysisRequest;
import com.orchestra.domain.dto.ReportRecommendations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ReportAnalystAgent extends BaseAgent<ReportAnalysisRequest, ReportRecommendations> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final PromptManagerService promptManagerService;
    private final ObjectMapper objectMapper;

    public ReportAnalystAgent(ChatClient.Builder builder,
                              ChatMemory chatMemory,
                              PromptManagerService promptManagerService,
                              ObjectMapper objectMapper) {
        super(builder, chatMemory, "You are a QA Analyst.");
        this.promptManagerService = promptManagerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportRecommendations execute(ReportAnalysisRequest input) {
        BeanOutputConverter<ReportRecommendations> converter = new BeanOutputConverter<>(ReportRecommendations.class);

        String sysPromptTemplate = promptManagerService.getPrompt("analyst_system_prompt");
        String fullSystemPromptTemplate = sysPromptTemplate
                + """

                
                You must return the response strictly in the following JSON format, without any comments or text outside the JSON:
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(fullSystemPromptTemplate);
        String systemText = promptTemplate.render(Map.of("format", converter.getFormat()));

        // Low temperature for analytical precision
        OllamaChatOptions options = OllamaChatOptions.builder()
                .temperature(0.1)
                .build();

        String userMessage = buildUserMessage(input);

        log.info("ReportAnalystAgent: Analyzing report for scenario '{}'", input.scenarioName());

        String rawResponse = callLlm(systemText, userMessage, options);
        String cleanResponse = cleanResponse(rawResponse);

        return converter.convert(cleanResponse);
    }

    private String buildUserMessage(ReportAnalysisRequest request) {
        try {
            return "Analyze the following test run failure:\n" + objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request to JSON", e);
            return "Analyze the following test run failure:\n" + request.toString();
        }
    }

    private String cleanResponse(String response) {
        if (response == null) return "";
        String clean = THINK_PATTERN.matcher(response).replaceAll("").trim();
        if (clean.contains("```json")) {
            clean = clean.replace("```json", "").replace("```", "").trim();
        } else if (clean.contains("```")) {
            clean = clean.replace("```", "").trim();
        }
        int firstBrace = clean.indexOf('{');
        int lastBrace = clean.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            clean = clean.substring(firstBrace, lastBrace + 1);
        }
        return clean;
    }
}

