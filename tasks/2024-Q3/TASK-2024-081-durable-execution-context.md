---
id: TASK-2024-081
title: "Задача 3.1.6 (Backend): Реализация Durable Execution Context"
status: done
priority: high
type: task
estimate: 12h
created: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-048]
arch_refs: [ADR-0020]
---
## Описание
Модифицировать модель данных и логику `orchestra-executor` для хранения состояния выполнения в PostgreSQL, реализуя принцип "Durable State" из `ADR-0020: Durable Workflow Execution Engine`.

## Ключевые шаги
1. Добавить в таблицу `test_runs` поле `execution_context JSONB` для хранения переменных между шагами.
2. Добавить в `test_step_results` поля `input_context_snapshot JSONB` и `output_context_delta JSONB`.
3. В `orchestra-executor` после выполнения каждого шага реализовать транзакционное обновление `test_step_results` и `test_runs.execution_context`.
4. При старте выполнения `TestRun` или при восстановлении после сбоя `executor` должен уметь восстанавливать `ExecutionContext` из БД.

## Критерии приемки
- Состояние выполнения `TestRun` персистентно хранится в БД.
- `orchestra-executor` может продолжить выполнение с прерванного места (с `last_step+1`), следуя описанному в ADR принципу восстановления контекста.
