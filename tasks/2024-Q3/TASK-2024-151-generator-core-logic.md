---
id: TASK-2024-151
title: "Задача 4.4.13 (Backend): Гибридный маппинг и генерация данных (Hydration)"
status: todo
priority: high
type: feature
estimate: 16h
created: 2025-11-22
parents: [TASK-2024-031]
dependencies: [TASK-2024-150, TASK-2024-125]
arch_refs: [ADR-0019]
---

## Описание

Реализовать "умную" логику наполнения сценария. Генератор должен не просто создавать шаги, но и находить эндпоинты (используя имена и описания), генерировать данные через AI и сохранять их в DataSet.

## Ключевые шаги

1.  **Hybrid Endpoint Matcher (Реализация стратегии):**

    *   **Step 1: Exact Match.** Поиск по совпадению `operationId` (если указан в метаданных BPMN).

    *   **Step 2: Deterministic Fuzzy Match.** Нормализация строк. Поиск вхождения имени задачи BPMN в `path`, `summary` или `description` метода OpenAPI.

    *   **Step 3: Semantic AI Match.** Если уверенность низкая — вызов `MappingAgent`.

        *   *Важно:* Передавать агенту поле `documentation` из BPMN задачи и `description` из OpenAPI, чтобы AI мог сопоставить их по смыслу (например, "Создать клиента" -> `POST /users`).

2.  **Data Hydration Pipeline (в `ProcessToScenarioGenerator`):**

    *   После генерации структуры сценариев собрать все шаги, требующие тела запроса.

    *   Вызвать `AiService.generateDataForSuite` с переданным `environmentId`.

    *   Создать новый `TestDataSet` (scope=SUITE, origin=AI_GENERATED) и сохранить туда полученный JSON.

3.  **Linking:**

    *   Обновить шаги сценария: прописать в `inputTemplate.body` плейсхолдеры вида `{{data.stepAlias_request}}`.

4.  **Error Handling:**

    *   Если парсинг BPMN не удался — переводить сьют в `FAILED` и сохранять сообщение ошибки. Сьют не удалять.

## Критерии приемки

-   Созданный сьют имеет статус `READY`.

-   Привязка эндпоинтов работает даже если ID не совпадают, но смысл задачи (описание) соответствует методу API.

-   К сьюту привязан `TestDataSet` с валидным JSON.


