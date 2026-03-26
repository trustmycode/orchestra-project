package com.orchestra.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.domain.dto.AiDataTransferRequest;
import com.orchestra.domain.dto.AiDataTransferResponse;
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
public class DataTransferAgent extends BaseAgent<AiDataTransferRequest, AiDataTransferResponse> {

    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private final ObjectMapper objectMapper;

    public DataTransferAgent(ChatClient.Builder builder,
                             ChatMemory chatMemory,
                             ObjectMapper objectMapper) {
        super(builder, chatMemory, "You are a Data Integration Specialist. Your goal is to link data between process steps.");
        this.objectMapper = objectMapper;
    }

    @Override
    public AiDataTransferResponse execute(AiDataTransferRequest input) {
        BeanOutputConverter<AiDataTransferResponse> converter = new BeanOutputConverter<>(AiDataTransferResponse.class);

        String systemText = """
                You are a Data Integration Specialist.
                Analyze the 'Target Step' requirements and the 'Available Outputs' from previous steps.
                Suggest a data mapping JSON where keys are fields in the Target Step input, and values are placeholders referencing previous steps.
                
                Use the format: {{step.STEP_ALIAS.response.FIELD_NAME}}
                
                You must return the response strictly in the following JSON format, without any comments or text outside the JSON:
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(systemText);
        String renderedSystem = promptTemplate.render(Map.of("format", converter.getFormat()));

        OllamaChatOptions options = OllamaChatOptions.builder()
                .temperature(0.1)
                .build();

        String userMessage;
        try {
            userMessage = "Suggest data mapping for:\n" + objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }

        String rawResponse = callLlm(renderedSystem, userMessage, options);
        String cleanResponse = THINK_PATTERN.matcher(rawResponse).replaceAll("").trim();
        if (cleanResponse.startsWith("```")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        }

        return converter.convert(cleanResponse);
    }
}


