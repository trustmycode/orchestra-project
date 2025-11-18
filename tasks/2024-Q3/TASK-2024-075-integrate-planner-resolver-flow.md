---
id: TASK-2024-075
title: "Задача 4.10.3 (Backend): Интеграция потока Planner → Resolver"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-072]
dependencies: [TASK-2024-073, TASK-2024-074]
arch_refs: [ADR-0019]
---
## Описание
Связать вызов `ai-service`, получение `DataPlan` и исполнение плана `DataResolverService` в рамках одного эндпоинта генерации данных `orchestra-api`.

## Ключевые шаги
1. Обновить `AiController`/`AiService` в `orchestra-api`, чтобы сначала запрашивать `DataPlan`.
2. Передавать `DataPlan` в `DataResolverService` и получать финальный JSON.
3. Вернуть результат клиенту и логировать весь поток для отладки.

## Критерии приемки
- Эндпоинт генерации делает последовательные шаги Planner → Resolver и возвращает валидный JSON без участия LLM на финальной стадии.
