---
id: TASK-2024-064
title: "Задача 4.7.3 (Backend): Реализация экспорта данных в orchestra-executor"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-061]
dependencies: [TASK-2024-062]
arch_refs: [ADR-0022]
---
## Описание
Доработать `orchestra-executor` для поддержки передачи данных между сценариями в рамках `SuiteRun`.

## Ключевые шаги
1. При старте `TestRun` инициализировать `ExecutionContext` данными из `SuiteRun.context`.
2. После выполнения каждого шага проверять наличие поля `exportAs` в метаданных.
3. Если `exportAs` указано, извлекать данные из ответа шага и выполнять атомарное обновление `SuiteRun.context` в БД.

## Критерии приемки
- Шаг с `exportAs` обновляет `SuiteRun.context`.
- Следующий сценарий может использовать эти данные через плейсхолдер `{{suite.*}}`.
