---
id: TASK-2024-032
title: "Задача 4.5 (AI): AI-аналитика отчетов"
status: backlog
priority: high
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-027]
dependencies: [TASK-2024-097, TASK-2024-098]
arch_refs: [ADR-0016, ADR-0025]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать функцию анализа отчетов о выполнении тестов с помощью `ai-service`. Логика строится на агентском подходе (`sgr-agent-core`), где агент получает цель «проанализируй TestRun» и использует инструменты (`findFailedSteps`, `getErrorDetails`, `getApiSchemaForStep`) для подготовки рекомендаций.

## Критерии приемки
- После завершения `TestRun` система отправляет его результаты в `ai-service`.
- `ai-service` анализирует ошибки и паттерны.
- В UI на странице отчета отображается блок с рекомендациями от AI (например, "предложить новые тестовые данные для покрытия граничных случаев" или "указать на возможную ошибку в сервисе X").
