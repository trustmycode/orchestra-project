---
id: TASK-2024-061
title: "Эпик 4.7: Реализация зависимостей между сценариями в SuiteRun"
status: done
priority: medium
type: feature
estimate: 40h
created: 2024-07-30
updated: 2025-11-21
parents: [TASK-2024-027]
children: [TASK-2024-062, TASK-2024-063, TASK-2024-064, TASK-2024-065]
dependencies: [TASK-2024-056]
arch_refs: [ADR-0022]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
  - {date: 2025-11-21, user: "@RoboticAI", action: "epic completed, all children done"}
---
## Описание
Реализовать механизм для управления зависимостями между `TestScenario` внутри одного `SuiteRun`. Это включает зависимости по потоку управления (один сценарий запускается после другого) и по данным (сценарий использует данные, экспортированные предыдущим).

## Критерии приемки
- Система может выполнять наборы сценариев, в которых определены зависимости.
- Данные могут передаваться между сценариями в рамках одного `SuiteRun`.
- Пользователь может определять эти зависимости в UI.
