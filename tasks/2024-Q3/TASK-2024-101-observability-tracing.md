---
id: TASK-2024-101
title: "Задача 4.3.3 (Backend): Внедрение распределенной трассировки"
status: backlog
priority: medium
type: task
estimate: 8h
created: 2024-07-30
parents: [TASK-2024-030]
---
## Описание
Настроить распределенную трассировку с использованием OpenTelemetry для отслеживания запросов между `orchestra-web`, `orchestra-api`, `ai-service` и `orchestra-executor` (через RabbitMQ).

## Ключевые шаги
1.  Добавить зависимости OpenTelemetry в бэкенд-сервисы.
2.  Настроить OTLP-экспортер для отправки трейсов в коллектор (например, Jaeger).
3.  Обеспечить корректную передачу контекста трассировки через HTTP-заголовки и заголовки сообщений RabbitMQ.

## Критерии приемки
-   В Jaeger (или аналогичном UI) можно увидеть полный трейс, который начинается с запроса в `orchestra-api`, проходит через `ai-service` и завершается в `orchestra-executor`.

