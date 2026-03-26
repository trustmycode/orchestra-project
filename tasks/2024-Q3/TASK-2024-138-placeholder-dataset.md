---
id: TASK-2024-141
title: "Задача 4.10.15 (Backend): Создание Placeholder TestDataSet для асинхронной генерации"
status: todo
priority: high
type: feature
estimate: 4h
created: 2025-11-21
parents: [TASK-2024-133]
---

## Описание

Чтобы UI мог немедленно отобразить результат запуска генерации, бэкенд должен создавать "заглушку" (`placeholder`) сущности `TestDataSet` в момент старта задачи, а не после ее завершения.

## Техническое решение

1.  **Расширение модели `TestDataSet`:**
    *   Добавить поле `status` (VARCHAR): `GENERATING`, `READY`, `FAILED`.
    *   Добавить поле `generationJobId` (VARCHAR или UUID) для связи с асинхронной задачей.
    *   Сделать поле `data` (JSONB) необязательным при создании (`nullable`).

2.  **Изменение API `POST /api/v1/ai/jobs/generate`:**
    *   **Вход:** Принимает те же параметры (контекст, инструкции).
    *   **Логика:**
        1.  Создает новую запись `TestDataSet` в БД со статусом `GENERATING` и пустым полем `data`. Генерирует имя (например, "AI Generated Data Set [timestamp]").
        2.  Запускает `@Async` задачу (логику из TASK-132/136/137), передавая в нее `ID` нового датасета.
        3.  **Немедленно** возвращает `HTTP 202 Accepted` с телом, содержащим созданную "пустую" сущность `TestDataSet`.
    *   **Выход:** `TestDataSetSummary` со статусом `GENERATING`.

## Критерии приемки

- [ ] Вызов `POST /ai/jobs/generate` мгновенно создает запись в таблице `test_data_sets` со статусом `GENERATING`.
- [ ] API сразу возвращает `202 Accepted` и JSON созданного объекта.

