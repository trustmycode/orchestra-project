---
id: TASK-2024-022
title: "Эпик 3.1: Реализация отказоустойчивого Execution Engine (Durable Workflow)"
status: backlog
priority: high
type: feature
estimate: 48h
created: 2024-07-29
updated: 2024-07-30
parents: [TASK-2024-021]
children: [TASK-2024-048, TASK-2024-049, TASK-2024-050, TASK-2024-051, TASK-2024-052, TASK-2024-081, TASK-2024-082, TASK-2024-083]
arch_refs: [ARCH-execution-engine]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "decomposed into sub-tasks"}
---
## Описание
Реализовать отказоустойчивый и масштабируемый `Execution Engine` на основе паттерна "Durable Workflow". Состояние прогона (`ExecutionContext`) будет храниться в PostgreSQL, а RabbitMQ будет использоваться как триггер для безсостоящих воркеров `orchestra-executor`.

## Критерии приемки
- Все дочерние задачи выполнены.
- Сбой и перезапуск воркера `orchestra-executor` не приводит к потере или некорректному завершению `TestRun`.
- Система способна обрабатывать "зависшие" прогоны.
