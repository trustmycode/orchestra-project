---
id: TASK-2024-063
title: "Задача 4.7.2 (Backend): Реализация SuiteRun Orchestrator"
status: done
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-061]
dependencies: [TASK-2024-058, TASK-2024-062]
arch_refs: [ADR-0022]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "implemented orchestrator logic"}
---
## Описание
Реализовать сервис-оркестратор, который управляет жизненным циклом `SuiteRun` с учетом зависимостей между сценариями.

## Ключевые шаги
1. Модифицировать `SuiteRunService` для создания `TestRun` со статусом `PENDING`.
2. Создать `SuiteRunOrchestratorService`, который запускается по расписанию или по событию.
3. Реализовать логику анализа графа зависимостей и отправки готовых к выполнению `TestRun` в очередь RabbitMQ.

## Критерии приемки
- При запуске `SuiteRun` в очередь попадают только сценарии без зависимостей.
- После успешного завершения сценария в очередь попадают зависящие от него сценарии.
