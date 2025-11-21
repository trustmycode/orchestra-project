---
id: TASK-2024-062
title: "Задача 4.7.1 (Backend): Модель данных для зависимостей и контекста"
status: done
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2025-11-21
parents: [TASK-2024-061]
arch_refs: [ADR-0022]
audit_log:
  - {date: 2025-11-21, user: "@RoboticAI", action: "implemented model changes and migrations"}
---
## Описание
Обновить доменную модель и схему БД для поддержки зависимостей между сценариями и общего контекста `SuiteRun`.

## Ключевые шаги
1. Добавить поле `dependsOn` (JSONB) в `TestScenario` или создать отдельную связующую таблицу.
2. Добавить поле `context` (JSONB) в таблицу `suite_runs`.
3. Добавить поле `exportAs` (JSONB) в метаданные `ScenarioStep`.
4. Создать миграцию БД для этих изменений.

## Критерии приемки
- Схема БД обновлена и поддерживает новые поля.
- JPA-сущности отражают новые поля и связи.
