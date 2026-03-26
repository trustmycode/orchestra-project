package com.orchestra.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.ai.service.PromptManagerService;
import com.orchestra.domain.dto.SuiteAnalysisRequest;
import com.orchestra.domain.dto.SuiteContextPlan;
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
public class SuiteLinkerAgent extends BaseAgent<SuiteAnalysisRequest, SuiteContextPlan> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final PromptManagerService promptManagerService;
    private final ObjectMapper objectMapper;

    public SuiteLinkerAgent(ChatClient.Builder builder,
                            ChatMemory chatMemory,
                            PromptManagerService promptManagerService,
                            ObjectMapper objectMapper) {
        super(builder, chatMemory, "You are a Suite Architect.");
        this.promptManagerService = promptManagerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SuiteContextPlan execute(SuiteAnalysisRequest input) {
        BeanOutputConverter<SuiteContextPlan> converter = new BeanOutputConverter<>(SuiteContextPlan.class);

        String sysPromptTemplate = promptManagerService.getPrompt("suite_linker_system_v1");
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
            StringBuilder sb = new StringBuilder("Analyze the following scenario requirements and link them into a global suite context:\n");
            if (input.instructions() != null && !input.instructions().isBlank()) {
                sb.append("Keep in mind the user's primary goal: '").append(input.instructions()).append("'. This goal should guide your decisions on which variables are most important to link.\n");
            }
            sb.append(objectMapper.writeValueAsString(input));
            userMessage = sb.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String rawResponse = callLlm(systemText, userMessage, options);
        String cleanResponse = THINK_PATTERN.matcher(rawResponse).replaceAll("").trim();
        
        if (cleanResponse.startsWith("```")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        }

        return converter.convert(cleanResponse);
    }
}

