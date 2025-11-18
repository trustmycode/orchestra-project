---
id: TASK-2024-080
title: "Задача 3.2.4 (Backend): Корреляция и метрики для ASSERTION-шагов"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-023, TASK-2024-024]
dependencies: [TASK-2024-055]
arch_refs: [ADR-0021]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Обеспечить изоляцию тестов и сбор метрик "settling time" в DB/Kafka ASSERTION-плагинах с помощью `correlationId` и Prometheus (см. `ADR-0021`).

## Ключевые шаги
1. Генерировать уникальный `correlationId` для каждого `TestRun` и автоматически добавлять его во все ACTION-шага (HTTP заголовок, Kafka заголовок/ключ).
2. DB и Kafka ASSERTION должны использовать `correlationId` при построении запросов/фильтров (для Kafka — отдельный `consumer group` на `TestRun`).
3. Каждому ASSERTION-плагину добавить измерение времени до успешного выполнения условия (`timeToSuccess`) и сохранять его в `TestStepResult` + экспортировать в Prometheus.

## Критерии приемки
- Параллельные тестовые прогоны не влияют друг на друга благодаря `correlationId`.
- Для ASSERTION-шагов доступны метрики `timeToSuccess` в отчёте и в Prometheus.
