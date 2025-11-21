---
id: TASK-2024-124
title: "Задача: Рефакторинг AiService для вызова ai-service и подготовки контекста"
status: completed
priority: high
type: refactoring
estimate: 8h
created: 2024-07-31
updated: 2024-07-31
parents: [TASK-2024-037]
dependencies: [TASK-2024-123]
arch_refs: [ADR-0019]
audit_log:
  - {date: 2024-07-31, user: "@RoboticArchitect", action: "created"}
---

## Описание

Реализовать "Planner" часть потока генерации данных в `orchestra-api`. Это включает замену старой заглушки на логику сбора полного бизнес-контекста и вызов `ai-service` для получения `DataPlan`.

## Ключевые шаги

1.  **Создание метода:** В `AiService.java` создать новый метод `generateData(AiGenerateDataRequest request)`.
2.  **Сбор контекста:** Внутри нового метода реализовать логику сбора контекста:
    - Извлечь `tenantId` из `TenantContext`.
    - На основе `scenarioId` и `stepId` из запроса, загрузить из БД сущности `TestScenario` и `ScenarioStep`.
    - Сформировать `Map<String, Object>` со всей необходимой информацией (`tenantId`, `scenarioName`, `stepName`, `endpointRef` и т.д.).
3.  **Вызов `ai-service`:** Отправить собранный контекст POST-запросом на эндпоинт `ai-service`.
4.  **Обработка ответа:** Десериализовать ответ от `ai-service`, который представляет собой `DataPlan`.
5.  **Очистка:** Пометить старый метод `generateSimpleData` как `@Deprecated` или удалить его.

## Критерии приемки

- `ai-service` получает POST-запрос с полным бизнес-контекстом, включая `tenantId`.
- Метод `generateData` успешно получает и десериализует `DataPlan` для передачи на следующий этап.

  - {date: 2025-11-21, user: "@RoboticSeniorDev", action: "completed"}