---
id: TASK-2024-051
title: "Задача 3.1.4 (Backend): Реализация потребителя задач в `orchestra-executor`"
status: completed
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
Реализовать в `orchestra-executor` listener, который будет обрабатывать задачи из очереди.

## Ключевые шаги
1. Создать сервис `TestRunExecutorService`.
2. Создать `JobListener` с методом, аннотированным `@RabbitListener(queues = "run_jobs_queue")`.
3. В listener'е:
   a. Принять сообщение с `testRunId`.
   b. Найти `TestRun` в БД и обновить его статус на `IN_PROGRESS`.
   c. Вызвать `TestRunExecutorService` для выполнения основной логики.
   d. Обернуть вызов в `try-catch` для обработки ошибок и отправки в DLQ в случае сбоя.

## Критерии приемки
- `orchestra-executor` успешно принимает сообщение из очереди.
- Статус `TestRun` в БД обновляется на `IN_PROGRESS`.
