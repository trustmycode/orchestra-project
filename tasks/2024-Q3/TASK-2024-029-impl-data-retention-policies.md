---
id: TASK-2024-029
title: "Задача 4.2 (Backend): Политики хранения данных (Retention)"
status: backlog
priority: high
type: tech_debt
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-027]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать механизм автоматического удаления или архивирования старых данных (`TestRun` и связанных результатов) для контроля роста базы данных.

## Критерии приемки
- Реализована фоновая задача (cron job), которая периодически удаляет `TestRun` старше N дней.
- Срок хранения (N) можно конфигурировать на уровне арендатора.
