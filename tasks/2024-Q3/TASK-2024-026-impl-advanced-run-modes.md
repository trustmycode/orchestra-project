---
id: TASK-2024-026
title: "Задача 3.5 (Backend/Frontend): Продвинутые режимы запуска (FROM_STEP, SINGLE_STEP)"
status: backlog
priority: medium
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-021]
arch_refs: [ARCH-execution-engine]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать продвинутые режимы запуска тестов для удобства отладки.

## Критерии приемки
- (Backend) `Execution Engine` поддерживает режим `FROM_STEP`, который переиспользует результаты предыдущего запуска до указанного шага.
- (Backend) `Execution Engine` поддерживает режим `SINGLE_STEP` для выполнения только одного шага.
- (Frontend) В UI можно запустить тест в этих режимах (например, из контекстного меню шага).
