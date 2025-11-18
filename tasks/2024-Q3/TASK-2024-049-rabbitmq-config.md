---
id: TASK-2024-049
title: "Задача 3.1.2 (Backend): Конфигурация RabbitMQ"
status: backlog
priority: high
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-048]
arch_refs: [ARCH-execution-engine]
---
## Описание
Настроить RabbitMQ в `orchestra-api` и `orchestra-executor`.

## Ключевые шаги
1. Добавить зависимость `spring-boot-starter-amqp` в оба сервиса.
2. В `orchestra-api` создать `RabbitMQConfig`, который объявляет очередь `run_jobs_queue` и Dead Letter Queue (DLQ) `run_jobs_dlq`.
3. Настроить `application.properties` в обоих сервисах для подключения к RabbitMQ.

## Критерии приемки
- При старте `orchestra-api` в RabbitMQ автоматически создаются необходимые очереди и обменники.
