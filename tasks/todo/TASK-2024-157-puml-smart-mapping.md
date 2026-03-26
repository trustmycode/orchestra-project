---
id: TASK-2024-157
title: "Задача 4.14.6 (Backend): Контекстный маппинг по участникам (Participant Mapping)"
status: todo
priority: medium
type: feature
estimate: 6h
created: 2025-11-22
parents: [TASK-2024-104]
dependencies: [TASK-2024-151, TASK-2024-155]
arch_refs: [ADR-0019]
---

## Описание

Повысить точность маппинга для Sequence-диаграмм, используя информацию об участниках взаимодействия (кто и к кому обращается).

## Ключевые шаги

1.  **Spec Binding Logic:**

    *   В `ScenarioSuiteGenerateRequest` приходит карта `specBindings` (например, `{"OrderService": "spec-uuid-123"}`).

2.  **Endpoint Matcher Update:**

    *   В методе `match(GraphNode node)` извлекать из метаданных узла поле `target` (имя получателя сообщения, например "OrderService").

    *   Если для этого `target` в `specBindings` есть привязка к конкретной `ProtocolSpec`, то **искать эндпоинт ТОЛЬКО в этой спецификации**.

3.  **Optimization:**

    *   Это значительно сокращает пространство поиска и устраняет коллизии (когда одинаковый метод `POST /create` есть в разных микросервисах).

## Критерии приемки

-   Если в диаграмме есть `Client -> Billing: Pay` и `Client -> Warehouse: Pay`, система корректно маппит их на разные спецификации API, не путая эндпоинты.

