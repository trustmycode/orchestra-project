---
id: TASK-2024-020
title: "Задача 2.5 (Backend/Frontend): Реализация ScenarioSuite"
status: backlog
priority: medium
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-015]
arch_refs: [ARCH-data-model]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать сущность `ScenarioSuite` для группировки нескольких `TestScenario`, относящихся к одному бизнес-процессу.

## Критерии приемки
- (Backend) Реализованы CRUD API для `ScenarioSuite`.
- (Frontend) В UI можно создавать `ScenarioSuite` и добавлять в него сценарии.
- (Frontend) Реализован просмотр списка сценариев в рамках одного `ScenarioSuite`.
