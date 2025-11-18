---
id: TASK-2024-050
title: "Задача 3.1.3 (Backend): Реализация продюсера задач в `orchestra-api`"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-049]
arch_refs: [ARCH-execution-engine]
---
## Описание
Изменить `TestRunService` в `orchestra-api` так, чтобы он отправлял задачу в RabbitMQ вместо синхронного выполнения.

## Ключевые шаги
1. Внедрить `RabbitTemplate` в `TestRunService`.
2. Изменить метод `createAndRunTest`:
   a. Создать `TestRun` со статусом `QUEUED`.
   b. Отправить сообщение, содержащее `testRunId`, в `run_jobs_queue`.
   c. Удалить старую логику синхронного выполнения.
3. Вернуть пользователю `TestRunSummary` со статусом `QUEUED`.

## Критерии приемки
- Вызов `POST /testruns` приводит к появлению сообщения в `run_jobs_queue`.
- API отвечает быстро, не дожидаясь выполнения теста.
