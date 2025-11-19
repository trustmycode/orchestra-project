---
id: TASK-2024-100
title: "Задача 4.3.2 (Backend): Экспорт метрик в Prometheus"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
parents: [TASK-2024-030]
---
## Описание
Интегрировать Micrometer в бэкенд-сервисы для сбора и экспорта метрик в формате Prometheus.

## Ключевые шаги
1.  Добавить зависимость `spring-boot-starter-actuator` и `micrometer-registry-prometheus`.
2.  Реализовать сбор ключевых бизнес-метрик:
    *   `orchestra_testrun_total` (с тегами `tenant`, `status`)
    *   `orchestra_step_duration_ms` (гистограмма, с тегами `channelType`, `status`)
    *   `orchestra_assertion_settling_seconds` (гистограмма)
3.  Настроить `docker-compose.yml` для сбора метрик Prometheus'ом.

## Критерии приемки
-   Эндпоинт `/actuator/prometheus` доступен на всех сервисах.
-   В Prometheus поступают кастомные бизнес-метрики.

