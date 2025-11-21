package com.orchestra.ai.agent;

/**
 * Базовый интерфейс для AI-агентов.
 *
 * @param <I> Тип входных данных
 * @param <O> Тип выходных данных
 */
public interface AiAgent<I, O> {
    O execute(I input);
}

