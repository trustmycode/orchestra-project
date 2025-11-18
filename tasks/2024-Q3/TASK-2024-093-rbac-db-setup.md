---
id: TASK-2024-093
title: "Задача 4.1.1 (Backend/DB): Настройка RLS и ролей в PostgreSQL"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-028]
arch_refs: [ADR-0012, ARCH-security-multitenancy]
audit_log:
  - {date: 2024-07-30, user: "@AI-Codex", action: "created"}
---
## Описание
Подготовить базу данных к безопасной мультиарендности, настроив роли приложения и включив Row-Level Security для всех таблиц с полем `tenant_id` в соответствии с решениями `ADR-0012`.

## Ключевые шаги
1. Создать миграцию, которая добавляет роли `orchestra_app` (без `BYPASSRLS`) и `orchestra_admin` (с `BYPASSRLS`) с минимально необходимыми правами.
2. В той же миграции включить RLS на таблицах с `tenant_id` и описать политики, ограничивающие доступ условием `tenant_id = current_setting('app.current_tenant')::uuid`.
3. Обновить `docker-compose.yml` и application properties, чтобы основное приложение подключалось под ролью `orchestra_app`.
4. Подготовить smoke-тест/psql-скрипт, подтверждающий, что без установки `app.current_tenant` данные недоступны.

## Критерии приемки
- Роли и права в PostgreSQL соответствуют описанной схеме и проверены миграциями.
- Для всех таблиц с `tenant_id` включен RLS с корректными политиками.
- Приложение, запущенное с ролью `orchestra_app`, не читает данные без установки `app.current_tenant`.
