---
id: TASK-2024-119
title: "Задача 6.2 (Backend): Реализация DbSecretProvider в Executor"
status: backlog
priority: high
type: task
estimate: 8h
created: 2025-11-21
parents: [TASK-2024-117]
dependencies: [TASK-2024-118]
arch_refs: [ARCH-execution-engine]
---

## Описание

Реализовать логику подстановки секретов из БД в момент выполнения теста.

## Ключевые шаги

1.  **Refactoring:** Изменить интерфейс `SecretProvider`. Метод `resolve(String value)` должен принимать контекст тенанта: `resolve(String value, UUID tenantId)`.

2.  **DbSecretProvider:**

    - Реализовать класс `DbSecretProvider`, который парсит строку на наличие паттерна `{{secret.KEY}}`.

    - При нахождении паттерна делать запрос в `TenantSecretRepository` по `key` и `tenantId`.

    - Заменять плейсхолдер на реальное значение.

3.  **Integration:**

    - Обновить `ConnectionManager` (и другие места использования), чтобы они передавали `tenantId` из `TestRun` в метод `resolve`.

    - Обеспечить приоритет: сначала ищем в БД (`{{secret.*}}`), если не нашли или формат `{{env.*}}` — фоллбек на переменные окружения.

## Критерии приемки

- Executor успешно резолвит `{{secret.DB_PASS}}` при подключении к базе.
- Если секрет не найден, выбрасывается понятное исключение.

