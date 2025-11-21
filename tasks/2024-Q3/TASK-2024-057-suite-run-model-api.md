---
id: TASK-2024-057
title: "Задача 4.6.1 (Backend): Модель данных и API для SuiteRun"
status: done
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-056]
arch_refs: [ADR-0022]
---
## Описание
Создать доменные сущности `SuiteRun` и добавить связь в `TestRun`. Реализовать API-эндпоинт для инициации запуска `SuiteRun`.

## Ключевые шаги
1. Создать JPA-сущность `SuiteRun` и обновить `TestRun`.
2. Создать миграцию БД для новой таблицы `suite_runs` и колонки `suite_run_id` в `test_runs`.
3. Реализовать `SuiteRunController` с эндпоинтом `POST /suite-runs`.
4. Реализовать `SuiteRunService`, который создает `SuiteRun`, дочерние `TestRun` (со статусом `PENDING`) и передает управление `Scheduler`'у.

## Критерии приемки
- Вызов `POST /suite-runs` создает в БД одну запись `SuiteRun` и N записей `TestRun` со статусом `PENDING`.
