---
id: TASK-2024-118
title: "Задача 6.1 (Backend): API и БД для секретов"
status: backlog
priority: high
type: task
estimate: 8h
created: 2025-11-21
parents: [TASK-2024-117]
arch_refs: [ARCH-data-model]
---

## Описание

Создать сущность `TenantSecret`, миграцию БД и REST API для управления секретами.

## Ключевые шаги

1.  **Domain:**

    - Создать сущность `TenantSecret` (id, tenantId, key, value, description).

    - Добавить уникальный констрейнт на `(tenant_id, key)`.

2.  **Migration:**

    - Создать Flyway скрипт `V8__Add_tenant_secrets.sql`.

3.  **API:**

    - Реализовать `SecretController` (`GET`, `POST`, `PUT`, `DELETE` `/api/v1/secrets`).

    - В методах `GET` возвращать маскированное значение (например, `*****`) или не возвращать `value` вовсе (только метаданные), чтобы секрет нельзя было подсмотреть через UI после сохранения.

## Критерии приемки

- Можно сохранить секрет через API.
- Нельзя создать два секрета с одинаковым ключом в рамках одного тенанта.

