package com.orchestra.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.ai.service.PromptManagerService;
import com.orchestra.ai.service.ToolRegistry;
import com.orchestra.domain.dto.AiMappingRequest;
import com.orchestra.domain.dto.AiMappingResponse;
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
public class MappingAgent extends BaseAgent<AiMappingRequest, AiMappingResponse> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final PromptManagerService promptManagerService;
    private final ObjectMapper objectMapper;

    public MappingAgent(ChatClient.Builder builder,
                        ChatMemory chatMemory,
                        PromptManagerService promptManagerService,
                        ObjectMapper objectMapper,
                        ToolRegistry toolRegistry) {
        super(builder, chatMemory, "You are an API Mapping Agent.", toolRegistry.getGlobalTools());
        this.promptManagerService = promptManagerService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiMappingResponse execute(AiMappingRequest input) {
        BeanOutputConverter<AiMappingResponse> converter = new BeanOutputConverter<>(AiMappingResponse.class);

        String sysPromptTemplate = promptManagerService.getPrompt("mapping_agent_system_v1");
        String fullSystemPromptTemplate = sysPromptTemplate
                + """

                

                You must return the response strictly in the following JSON format, without any comments or text outside the JSON:
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(fullSystemPromptTemplate);
        String systemText = promptTemplate.render(Map.of("format", converter.getFormat()));

        OllamaChatOptions options = OllamaChatOptions.builder()
                .temperature(0.0) // Deterministic
                .build();

        String userMessage;
        try {
            userMessage = "Find the best API endpoint for the following task:\nTask Name: " + input.getTaskName() + "\nDescription: " + (input.getTaskDescription() != null ? input.getTaskDescription() : "") + "\n\nCandidates:\n" + objectMapper.writeValueAsString(input.getCandidates());
        } catch (Exception e) {
            log.error("Failed to serialize candidates", e);
            userMessage = "Find the best API endpoint for the following task:\nTask Name: " + input.getTaskName();
        }

        String rawResponse = callLlm(systemText, userMessage, options);
        String cleanResponse = THINK_PATTERN.matcher(rawResponse).replaceAll("").trim();
        
        if (cleanResponse.startsWith("```")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        }

        return converter.convert(cleanResponse);
    }
}

