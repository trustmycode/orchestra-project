---
id: TASK-2024-038
title: "Задача 2.2.5: Добавление `ai-service` в Docker Compose"
status: backlog
priority: medium
type: chore
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-033]
dependencies: [TASK-2024-035]
arch_refs: [ARCH-core-services]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Интегрировать новый `ai-service` в общее окружение для локальной разработки, обновив файлы `docker-compose.yml` и `docker-compose.override.yml`.

## Ключевые шаги
- Добавить определение сервиса `ai-service` в `infra/docker-compose.yml`, указав build context.
- Настроить `extra_hosts` для `ai-service`, чтобы он мог обращаться к Ollama, запущенному на хост-машине (`host.docker.internal`).
- Передать URL Ollama через переменную окружения `OLLAMA_BASE_URL`.
- Добавить `ai-service` в `infra/docker-compose.override.yml`, настроив монтирование `target/classes` для поддержки hot reload.

## Критерии приемки
- Команда `docker compose up` успешно запускает `ai-service` вместе с остальными компонентами стека.
- Сервис `orchestra-api` внутри Docker-сети может успешно отправлять запросы в `ai-service`.
