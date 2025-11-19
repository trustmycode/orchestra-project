---
id: ARCH-execution-engine
title: "Движок исполнения тестов (Durable Workflow Execution Engine)"
type: component
layer: application
owner: '@architect'
version: v2
status: planned
created: 2024-07-29
updated: 2024-07-30
tags: [executor, worker, rabbitmq, async, durable, stateless]
depends_on: [ARCH-core-services, ARCH-protocol-plugins]
referenced_by: []
---
## Контекст
Движок исполнения реализован на основе паттерна **"Durable Workflow"**, где состояние выполнения персистентно хранится в PostgreSQL, а воркеры являются безсостоятельными (stateless). Это обеспечивает высокую отказоустойчивость и масштабируемость.

## Структура и компоненты
*   **`Scheduler` (в `orchestra-api`)**: Центральный планировщик. Отвечает за применение квот, приоритетов и запуск задач. Переводит `TestRun` из `PENDING` в `QUEUED` и отправляет сообщение в RabbitMQ.
*   **Очередь задач (`run_jobs` в RabbitMQ)**: Используется как **триггер** для воркеров. Содержит только `testRunId`.
*   **`Executor` (воркер `orchestra-executor`)**: Безсостоящий исполнитель. Не хранит состояние между шагами.
*   **`Reaper` (фоновый процесс)**: Отслеживает и завершает "зависшие" прогоны.
*   **PostgreSQL (Источник правды)**: Хранит полное состояние каждого `TestRun`, включая `status`, `ExecutionContext`, `lock_until`, `heartbeat_at`.

## Поведение (Durable Workflow)
1.  **Планирование:** `Scheduler` выбирает `PENDING` `TestRun`, переводит его в `QUEUED` и отправляет `testRunId` в RabbitMQ.
2.  **Захват задачи:** Воркер получает `testRunId` и пытается "арендовать" задачу, выполняя атомарный `UPDATE test_runs SET status='IN_PROGRESS', lock_until=... WHERE id=... AND status='QUEUED'`.
3.  **Восстановление контекста:** Воркер загружает `ExecutionContext` из `test_runs.execution_context` и определяет, с какого шага продолжать (на основе `last_step`).
4.  **Выполнение шага:** Воркер выполняет один шаг, используя соответствующий `ProtocolPlugin`.
5.  **Сохранение состояния:** После каждого шага воркер открывает транзакцию и атомарно обновляет в PostgreSQL:
    *   Статус выполненного шага в `test_step_results`.
    *   `execution_context` в `test_runs` (применяя дельту от шага).
    *   `heartbeat_at` и `last_step` в `test_runs`.
6.  **Heartbeat:** Во время длительных шагов (polling) воркер периодически обновляет `heartbeat_at`.
7.  **Завершение:** После последнего шага воркер устанавливает финальный статус `TestRun` (`PASSED`/`FAILED`) и подтверждает (`ack`) сообщение в RabbitMQ.
8.  **Обработка сбоев:**
    *   **Сбой воркера:** Сообщение возвращается в очередь. Другой воркер "арендует" задачу, восстанавливает контекст и продолжает с прерванного места.
    *   **"Зависший" воркер:** `Reaper` обнаруживает прогон по устаревшему `heartbeat_at`, отменяет аренду и переводит `TestRun` в `FAILED_STUCK`.
