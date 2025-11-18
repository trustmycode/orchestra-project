---
id: TASK-2024-028
title: "Эпик 4.1: Реализация полной мультиарендной изоляции (RBAC + RLS)"
status: backlog
priority: high
type: feature
estimate: 24h
created: 2024-07-29
updated: 2024-07-30
parents: [TASK-2024-027]
children: [TASK-2024-093, TASK-2024-094, TASK-2024-095]
arch_refs: [ARCH-security-multitenancy, ADR-0020]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2024-07-30, user: "@AI-Codex", action: "обновлено описание эпика и добавлены дочерние задачи"}
---
## Описание
Реализовать полноценную ролевую модель доступа (RBAC) и строгую мультиарендную изоляцию данных, используя двухуровневый подход: фильтрацию на уровне приложения и Row-Level Security (RLS) в PostgreSQL с раздельными ролями `orchestra_app` и `orchestra_admin`.

## Критерии приемки
- Все дочерние задачи выполнены и покрывают фильтрацию, RLS и административные сценарии.
- Данные одного тенанта недоступны другому даже при ошибках приложения.
- RLS-политики и приложение используют `tenant_id` как обязательную часть всех ключей и фильтров.
