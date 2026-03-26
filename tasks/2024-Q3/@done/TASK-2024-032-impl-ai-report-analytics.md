---
id: TASK-2024-032
title: "Задача 4.5 (AI): Реализация ReportAnalystAgent на Spring AI"
status: backlog
priority: medium
type: feature
estimate: 16h
created: 2024-07-29
updated: 2025-11-21
parents: [TASK-2024-027]
dependencies: [TASK-2024-097, TASK-2024-116, TASK-2024-036]
arch_refs: [ADR-0031, ADR-0032, ADR-0016]
---
## Описание
Реализовать Java-агента `ReportAnalystAgent` в сервисе `ai-service` на базе фреймворка **Spring AI**.
Агент должен принимать данные о неудачном тестовом прогоне (`TestRun`), анализировать ошибки, логи и контекст, и возвращать структурированные рекомендации по исправлению.

В отличие от старого плана (sgr-agent-core), здесь мы используем **BeanOutputConverter** для получения строго типизированного ответа, который можно сразу отобразить в UI.

## Архитектура решения
1.  **Input:** JSON-структура с деталями упавших шагов (Step Alias, Error Message, Stacktrace, Last Payload).
2.  **Processing:**
    *   Загрузка системного промпта `analyst_system_prompt` из БД (`PromptManager`).
    *   Формирование контекста для LLM.
    *   Вызов модели (`gpt-oss:20b`) через `ChatClient`.
3.  **Output:** Объект `ReportRecommendations` (Java Record), содержащий списки улучшений для сценария, данных и спецификаций.

## Ключевые шаги

1.  **Domain & DTO:**
    *   Убедиться, что в `ai-service` есть DTO `ReportAnalysisRequest` (входные данные) и `ReportRecommendations` (выходные данные), соответствующие OpenAPI.
2.  **Agent Implementation:**
    *   Создать класс `ReportAnalystAgent` в `com.orchestra.ai.agent`.
    *   Использовать `BeanOutputConverter<ReportRecommendations>` для генерации схемы JSON и парсинга ответа.
    *   Настроить `ChatClient` с температурой 0.1 (для аналитики нужна точность).
3.  **Prompt Engineering:**
    *   Обновить/проверить промпт `analyst_system_prompt` в таблице `ai_prompts`. Он должен инструктировать модель искать корневую причину (Root Cause Analysis).
4.  **Controller:**
    *   Реализовать эндпоинт `POST /api/v1/ai/analyze-report` в `ai-service`.
5.  **Integration:**
    *   В `orchestra-api` добавить вызов этого агента. Можно сделать это асинхронно (при завершении теста со статусом FAILED) или синхронно по требованию пользователя (кнопка "Analyze with AI" в отчете).

## Критерии приемки
-   [ ] Агент реализован на Java + Spring AI.
-   [ ] При отправке JSON с ошибкой (например, "DB Timeout") агент возвращает JSON с рекомендацией (например, "Increase timeout in step settings or check DB load").
-   [ ] Ответ агента строго соответствует схеме `ReportRecommendations` и не требует ручного парсинга текста.