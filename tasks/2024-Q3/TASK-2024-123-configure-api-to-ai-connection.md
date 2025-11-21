---
id: TASK-2024-123
title: "Задача: Конфигурация подключения к ai-service в orchestra-api"
status: completed
priority: high
type: task
estimate: 2h
created: 2024-07-31
updated: 2024-07-31
parents: [TASK-2024-037]
arch_refs: [ARCH-core-services]
audit_log:
  - {date: 2024-07-31, user: "@RoboticArchitect", action: "created"}
  - {date: 2025-11-21, user: "@RoboticSeniorDev", action: "completed"}
---

## Описание

Настроить `orchestra-api` для взаимодействия с микросервисом `ai-service`. Это базовый шаг для обеспечения коммуникации между сервисами.

## Ключевые шаги

1.  **Конфигурация:** В `application.properties` сервиса `orchestra-api` добавить свойство `orchestra.ai-service.url` и задать ему значение (например, `http://ai-service:8080`).
2.  **HTTP-клиент:** Создать или убедиться в наличии `@Bean` для `RestTemplate` или `WebClient` в конфигурации `orchestra-api`.
3.  **Инъекция:** Внедрить `RestTemplate` в `AiService.java`.

## Критерии приемки

- Конфигурация URL вынесена в `application.properties` и может быть переопределена переменными окружения.
- `RestTemplate` успешно инъецируется в `AiService` и готов к использованию.

