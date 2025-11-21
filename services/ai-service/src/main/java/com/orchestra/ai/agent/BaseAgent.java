package com.orchestra.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * Абстрактная реализация агента с поддержкой памяти диалога и инструментов.
 *
 * @param <I> тип входных данных
 * @param <O> тип результата
 */
public abstract class BaseAgent<I, O> implements AiAgent<I, O> {

    protected final ChatClient chatClient;
    protected final String agentName;

    protected BaseAgent(ChatClient.Builder builder,
                        ChatMemory chatMemory,
                        String systemPrompt,
                        Object... tools) {
        this.agentName = systemPrompt;

        ChatClient.Builder configured = builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());

        // Регистрируем инструменты как Function/Tool callbacks по умолчанию
        if (tools != null && tools.length > 0) {
            configured = configured.defaultTools(tools);
        }

        this.chatClient = configured.build();
    }

    /**
     * Выполняет запрос к LLM.
     *
     * @param userMessage Сообщение пользователя
     * @return Ответ модели
     */
    protected String callLlm(String userMessage) {
        return callLlm(userMessage, "default");
    }

    /**
     * Выполняет запрос к LLM с указанием ID диалога.
     *
     * @param userMessage    Сообщение пользователя
     * @param conversationId ID диалога
     * @return Ответ модели
     */
    protected String callLlm(String userMessage, String conversationId) {
        return chatClient
                .prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * Вспомогательный вызов с кастомным system-prompt, user-сообщением и опциями модели.
     */
    protected String callLlm(String systemText,
                             String userMessage,
                             OllamaChatOptions options,
                             String conversationId) {

        var promptBuilder = chatClient
                .prompt()
                .system(systemText)
                .user(userMessage);

        if (options != null) {
            promptBuilder = promptBuilder.options(options);
        }

        if (conversationId != null) {
            promptBuilder = promptBuilder.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
        }

        return promptBuilder
                .call()
                .content();
    }

    protected String callLlm(String systemText,
                             String userMessage,
                             OllamaChatOptions options) {
        return callLlm(systemText, userMessage, options, null);
    }
}
