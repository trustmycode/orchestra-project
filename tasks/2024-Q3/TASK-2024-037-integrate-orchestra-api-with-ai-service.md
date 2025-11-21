---
id: TASK-2024-037
title: "Задача 2.2.4: Интеграция `orchestra-api` с `ai-service`"
status: backlog
priority: medium
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-033]
dependencies: [TASK-2024-036]
arch_refs: [ARCH-core-services]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Заменить существующую заглушку (`generateSimpleData`) в `com.orchestra.api.service.AiService` на реальную интеграцию с микросервисом `ai-service`.

## Ключевые шаги
1.  **Refactor `AiService.java`:**
    *   Внедрить `RestClient` или `WebClient`.
    *   Реализовать метод `generateDataPlan(ScenarioContext context)`, который отправляет POST запрос в `ai-service`.
2.  **Implement `DataResolver` flow:**
    *   Полученный от AI `DataPlan` (JSON с критериями) передать в `DataResolverService` (см. TASK-2024-073).
    *   Сформировать итоговый `TestDataSet`.
3.  **Config:**
    *   Добавить `orchestra.ai-service.url` в `application.properties`.

## Критерии приемки
*   При вызове эндпоинта генерации данных в логах видно обращение к `ai-service`.
*   Возвращаемые данные не являются хардкодом, а зависят от переданного контекста (или хотя бы генерируются LLM).
