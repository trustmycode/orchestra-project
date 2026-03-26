---
id: TASK-2024-150
title: "Задача 4.4.12 (Backend): API для асинхронной генерации Suite"
status: done
priority: high
type: feature
estimate: 6h
created: 2025-11-22
parents: [TASK-2024-031]
arch_refs: [ARCH-execution-engine]
---

## Описание

Необходимо перевести генерацию `ScenarioSuite` в асинхронный режим, чтобы избежать таймаутов при длительной работе AI и не блокировать пользователя.

## Ключевые шаги

1.  **Domain Model:**

    *   Добавить в сущность `ScenarioSuite` поле `generation_job_id` (UUID) и `status` (если еще нет, использовать статус джоба для отображения).

2.  **DTO:**

    *   Обновить `ScenarioSuiteGenerateRequest`, добавив поле `environmentId` (UUID, обязательное для режима "Ready to Run").

3.  **API:**

    *   Реализовать эндпоинт `POST /api/v1/scenario-suites/from-process/async`.

    *   Эндпоинт должен создавать запись `ScenarioSuite` со статусом `GENERATING`.

    *   Создавать `AiJob` через `AiJobService`.

    *   Возвращать `202 Accepted` с телом `{ suiteId, jobId }`.

4.  **Service:**

    *   Создать метод, помеченный `@Async`, который будет выполнять логику генерации (саму логику наполнения реализуем в следующей задаче, пока заглушка).

    *   Обеспечить перехват исключений: при ошибке статус `ScenarioSuite` должен меняться на `FAILED` (не удалять запись!), а в `AiJob` записываться текст ошибки.

## Критерии приемки

-   Вызов нового эндпоинта возвращает ID джоба и сьюта мгновенно.

-   В БД появляется запись сьюта со статусом `GENERATING`.

