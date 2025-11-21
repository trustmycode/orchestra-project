package com.orchestra.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.ai.service.PromptManagerService;
import com.orchestra.domain.dto.ScenarioAnalysisRequest;
import com.orchestra.domain.dto.ScenarioAnalysisResponse;
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
public class ScenarioAnalystAgent extends BaseAgent<ScenarioAnalysisRequest, ScenarioAnalysisResponse> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final PromptManagerService promptManagerService;
    private final ObjectMapper objectMapper;

    public ScenarioAnalystAgent(ChatClient.Builder builder,
                                ChatMemory chatMemory,
                                PromptManagerService promptManagerService,
                                ObjectMapper objectMapper) {
        super(builder, chatMemory, "You are a Scenario Analyst.");
        this.promptManagerService = promptManagerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ScenarioAnalysisResponse execute(ScenarioAnalysisRequest input) {
        BeanOutputConverter<ScenarioAnalysisResponse> converter = new BeanOutputConverter<>(ScenarioAnalysisResponse.class);

        String sysPromptTemplate = promptManagerService.getPrompt("scenario_analyst_system_v1");
        String fullSystemPromptTemplate = sysPromptTemplate
                + """

                

                You must return the response strictly in the following JSON format, without any comments or text outside the JSON:
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(fullSystemPromptTemplate);
        String systemText = promptTemplate.render(Map.of("format", converter.getFormat()));

        OllamaChatOptions options = OllamaChatOptions.builder()
                .temperature(0.1)
                .build();

        String userMessage;
        try {
            userMessage = "Analyze the following scenario:\n" + objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        log.info("ScenarioAnalystAgent: Analyzing scenario '{}'", input.scenarioName());

        String rawResponse = callLlm(systemText, userMessage, options);
        String cleanResponse = THINK_PATTERN.matcher(rawResponse).replaceAll("").trim();
        
        // Basic cleanup for markdown code blocks
        if (cleanResponse.startsWith("```")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        }

        return converter.convert(cleanResponse);
    }
}

