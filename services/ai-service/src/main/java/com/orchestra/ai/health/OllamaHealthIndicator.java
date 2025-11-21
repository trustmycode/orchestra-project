package com.orchestra.ai.health;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OllamaHealthIndicator implements HealthIndicator {

    private final ChatModel chatModel;

    @Override
    public Health health() {
        try {
            // Простой ping-запрос к модели для проверки доступности
            var response = chatModel.call(new Prompt("ping"));
            if (response != null && response.getResult() != null) {
                return Health.up()
                        .withDetail("status", "Connected to Ollama")
                        .build();
            } else {
                return Health.down().withDetail("error", "Empty response from Ollama").build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("error", "Failed to connect to Ollama").build();
        }
    }
}

