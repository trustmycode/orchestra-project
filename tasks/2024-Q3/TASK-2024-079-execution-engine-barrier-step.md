---
id: TASK-2024-079
title: "Задача 3.1.6 (Backend): Реализация шага `BARRIER` в Execution Engine"
status: backlog
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-052]
arch_refs: [ADR-0021]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Добавить новый тип шага `BARRIER`, который ожидает завершения указанных параллельных шагов, прежде чем продолжить выполнение сценария (см. `ADR-0021`).

## Ключевые шаги
1. Расширить модель `ScenarioStep`, добавив тип `BARRIER`, список `trackedSteps` и настройки таймаута.
2. В `orchestra-executor` реализовать переход шага в состояние `WAITING` и механизм отслеживания завершения `trackedSteps` (через события или периодический опрос `TestStepResult`).
3. При успешном завершении всех отслеживаемых шагов — переводить `BARRIER` в `PASSED`, при ошибке или таймауте — в `FAILED`.
4. Рассмотреть использование Project Loom (`StructuredTaskScope`) для удобного ожидания параллельных задач.

## Критерии приемки
- Сценарий с параллельными шагами и `BARRIER` корректно блокируется до завершения всех указанных шагов.
- Отчёт по `TestRun` отражает отдельный `BARRIER`-шаг со статусом и длительностью ожидания.
