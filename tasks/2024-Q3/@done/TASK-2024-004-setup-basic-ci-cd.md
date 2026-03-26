---
id: TASK-2024-004
title: "Задача 0.3: Настройка базового CI/CD"
status: done
priority: high
type: chore
estimate: 8h
created: 2024-07-29
updated: 2025-11-15
parents: [TASK-2024-001]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2025-11-15, user: "@codex", action: "status changed to done"}
---
## Описание
Настроить базовый пайплайн непрерывной интеграции и доставки.

## Критерии приемки
- Пайплайн запускается при пуше в ветку `develop`.
- Пайплайн включает шаги для сборки (build) и прогона тестов (lint, unit tests).
- (Опционально) Настроен деплой в dev-окружение.
