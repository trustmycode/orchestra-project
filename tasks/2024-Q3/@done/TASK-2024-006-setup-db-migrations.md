---
id: TASK-2024-006
title: "Задача 0.5: Настройка миграций БД (Flyway/Liquibase)"
status: done
priority: high
type: chore
estimate: 4h
created: 2024-07-29
updated: 2025-11-15
parents: [TASK-2024-001]
arch_refs: [ARCH-data-model]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2025-11-15, user: "@codex", action: "status changed to done"}
---
## Описание
Интегрировать инструмент для управления миграциями схемы базы данных (например, Flyway или Liquibase) в бэкенд-проект.

## Критерии приемки
- Инструмент миграций добавлен в зависимости проекта.
- Создана первая, "инициализирующая" миграция.
- Миграции автоматически применяются при старте приложения.
