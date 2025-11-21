---
id: TASK-2024-035
title: "Задача 2.2.2: Создание скелета проекта `ai-service`"
status: completed
priority: high
type: chore
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-033]
arch_refs: [ARCH-core-services]
audit_log:
  - {
      date: 2024-07-30,
      user: "@RoboticArchitect",
      action: "created with status backlog",
    }
  - {
      date: 2024-07-30,
      user: "@RoboticSeniorDev",
      action: "completed",
    }
---

## Описание

Создать новый Spring Boot проект для `ai-service` в директории `services/`, настроить базовые зависимости и структуру.

## Ключевые шаги

- Создать новый модуль Maven/Gradle `ai-service`.
- Добавить зависимости: `spring-boot-starter-web` и `spring-ai-ollama-spring-boot-starter`.
- Создать базовую структуру пакетов (controller, service).
- Создать Dockerfile для сервиса по аналогии с `orchestra-api`.
- Добавить `ai-service` в `build-backend` job в `.github/workflows/ci.yml`.

## Критерии приемки

- Проект `ai-service` успешно собирается через Maven.
- CI/CD пайплайн корректно собирает новый сервис.
