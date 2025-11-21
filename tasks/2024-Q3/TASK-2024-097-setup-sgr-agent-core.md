---
id: TASK-2024-097
title: "Задача 4.10.0 (Backend): Настройка Spring AI и базового Agent Runtime"
status: backlog
priority: high
type: chore
estimate: 6h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-072]
arch_refs: [ADR-0031]
---
## Описание
Подключить и настроить `spring-ai` в проекте `ai-service`. Реализовать базовые абстракции для работы агентов.

## Ключевые шаги
1.  Добавить зависимости `spring-ai-ollama-spring-boot-starter` (проверить совместимость версий).
2.  Настроить `ChatClient.Builder` с дефолтными параметрами (temperature=0.0 для детерминизма).
3.  Создать интерфейс `AiAgent<I, O>` и базовую реализацию цикла обработки сообщений (Message History management).
4.  Реализовать Health Check эндпоинт, проверяющий связь с Ollama.

## Критерии приемки
-   `ai-service` успешно стартует и соединяется с Ollama.
-   Доступен базовый интерфейс для создания агентов.
