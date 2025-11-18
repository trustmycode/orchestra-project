---
id: TASK-2024-072
title: "Эпик 4.10 (AI): Внедрение генерации данных по паттерну 'LLM as Planner + Data Resolvers'"
status: backlog
priority: high
type: feature
estimate: 48h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-027]
children: [TASK-2024-097, TASK-2024-073, TASK-2024-098, TASK-2024-074, TASK-2024-075, TASK-2024-076, TASK-2024-077]
dependencies: [TASK-2024-036]
arch_refs: [ADR-0019]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Реализовать механизм генерации тестовых данных, в котором `ai-service` формирует `DataPlan`, а `Data Resolver` в `orchestra-api` исполняет его и возвращает реальные данные из тестовой среды.

## Критерии приемки
- Все дочерние задачи выполнены.
- Эндпоинт генерации данных возвращает валидный JSON, построенный на основе реальных идентификаторов из тестовой БД.
