---
id: TASK-2024-003
title: "Задача 0.2: Развертывание базовой инфраструктуры через Docker Compose"
status: done
priority: high
type: chore
estimate: 8h
created: 2024-07-29
updated: 2025-11-15
parents: [TASK-2024-001]
arch_refs: [ARCH-core-services]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2025-11-15, user: "@codex", action: "status changed to done"}
---
## Описание
Создать файл `docker-compose.yml` для локальной разработки, который поднимает все необходимые инфраструктурные компоненты.

## Критерии приемки
- `docker-compose.yml` содержит сервисы: PostgreSQL, RabbitMQ, Keycloak (для dev).
- Команда `docker compose up` успешно запускает все сервисы без ошибок.
- Данные PostgreSQL сохраняются между перезапусками (используя volumes).
- RabbitMQ Management UI доступен.
