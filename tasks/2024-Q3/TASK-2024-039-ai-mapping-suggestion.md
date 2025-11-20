---
id: TASK-2024-039
title: "Задача 4.6 (AI): Агент для сопоставления BPMN и API (Mapping Agent)"
status: backlog
priority: low
type: feature
estimate: 12h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-027]
dependencies: [TASK-2024-097, TASK-2024-098, TASK-2024-031]
arch_refs: [ADR-0025]
---
## Описание
Реализовать агента, который помогает сопоставить задачу BPMN с эндпоинтом OpenAPI.

## Ключевые шаги
1.  Добавить в `AgentToolbox` инструменты:
    *   `searchEndpoints(keyword, method)`: Поиск по спецификации.
    *   `getEndpointDetails(operationId)`: Получение полного описания.
2.  Агент получает описание задачи (из BPMN) и ищет подходящий эндпоинт, используя инструменты фильтрации и семантического сравнения.

## Критерии приемки
-   Генератор сценариев использует агента для автоматического маппинга, если нет явных ID.
