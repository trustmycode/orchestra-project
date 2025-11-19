---
id: TASK-2024-082
title: "Задача 3.1.7 (Backend): Реализация механизма аренды (Lease) и Heartbeat"
status: done
priority: high
type: task
estimate: 12h
created: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-051]
arch_refs: [ADR-0020]
---
## Описание
Внедрить механизм аренды для захвата задач и "heartbeat" для отслеживания "живых" прогонов, как предписывает `ADR-0020` для отказоустойчивого движка.

## Ключевые шаги
1. **Lease:**
   - Добавить в `test_runs` поля `locked_by` (id воркера) и `lock_until` (timestamp).
   - В `JobListener` (`orchestra-executor`) заменить `SELECT FOR UPDATE` на атомарный `UPDATE ... WHERE ... RETURNING *` для захвата "аренды" на `TestRun`.
2. **Heartbeat & Reaper:**
   - Добавить в `test_runs` поле `heartbeat_at`. Воркер должен обновлять его каждые N секунд во время выполнения.
   - Создать фоновую задачу (`Reaper`) в `orchestra-executor` или `orchestra-api`, которая ищет "зависшие" прогоны (где `heartbeat_at` устарел) и переводит их в статус `FAILED_STUCK` или `RETRY`.

## Критерии приемки
- Два воркера не могут одновременно выполнять один и тот же `TestRun` благодаря lease-паттерну.
- "Зависший" `TestRun` автоматически обнаруживается и помечается как сбойный через heartbeat/reaper, как описано в ADR.
