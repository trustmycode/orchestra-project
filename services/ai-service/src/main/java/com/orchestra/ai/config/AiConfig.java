package com.orchestra.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orchestra.ai.service.ToolRegistry;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ToolRegistry toolRegistry) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.0)
                        .build())
                // Все глобальные инструменты доступны по умолчанию
                .defaultTools(toolRegistry.getGlobalTools())
                .build();
    }
}
