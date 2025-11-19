---
id: TASK-2024-099
title: "Задача 4.3.1 (Backend): Внедрение структурированного логирования"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
parents: [TASK-2024-030]
---
## Описание
Настроить во всех бэкенд-сервисах (`orchestra-api`, `executor`, `ai-service`) структурированное логирование в формате JSON.

## Ключевые шаги
1.  Настроить Logback (или другой logging framework) для вывода логов в JSON.
2.  Внедрить MDC (Mapped Diagnostic Context) для автоматического добавления в каждый лог ключевых идентификаторов: `tenantId`, `runId`, `correlationId`.
3.  Обеспечить, чтобы логи не содержали чувствительных данных (секретов, PII).

## Критерии приемки
-   Логи всех сервисов пишутся в `stdout` в формате JSON.
-   Каждая запись лога в рамках обработки запроса содержит `tenantId` и `correlationId`.

