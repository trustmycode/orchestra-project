---
id: TASK-2024-014
title: "Задача 1.7 (Frontend): UI для запуска и просмотра отчета"
status: backlog
priority: high
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-007]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Создать в веб-интерфейсе возможность запустить сценарий и просмотреть базовый отчет о его выполнении.

## Критерии приемки
- На странице сценария есть кнопка "Запустить".
- После запуска пользователь перенаправляется на страницу `TestRun`.
- На странице `TestRun` отображается список выполненных шагов, их статусы (PASSED/FAILED) и длительность.
