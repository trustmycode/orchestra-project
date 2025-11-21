---
id: TASK-2024-125
title: "Задача: Интеграция DataResolverService в поток генерации данных"
status: backlog
priority: high
type: task
estimate: 6h
created: 2024-07-31
updated: 2024-07-31
parents: [TASK-2024-037]
dependencies: [TASK-2024-124, TASK-2024-073]
arch_refs: [ADR-0019]
audit_log:
  - {date: 2024-07-31, user: "@RoboticArchitect", action: "created"}
---

## Описание

Реализовать "Resolver" часть потока в `orchestra-api`, завершив сквозную интеграцию. Это включает передачу `DataPlan` в `DataResolverService` и формирование финального ответа для UI.

## Ключевые шаги

1.  **Интеграция:** В методе `generateData` (`AiService.java`), после получения `DataPlan` от `ai-service`, вызвать метод `dataResolverService.resolve(dataPlan, request.getEnvironmentId())`.
2.  **Формирование ответа:**
    - Получить от `DataResolverService` финальный JSON с реальными данными.
    - Сформировать объект `AiGenerateDataResponse`, заполнив поле `data` и, опционально, поле `notes` (из ответа `ai-service`).
3.  **Контроллер:** Убедиться, что `AiController` корректно вызывает обновленный `AiService` и возвращает `AiGenerateDataResponse`.
4.  **Тестирование:** Написать интеграционный тест, который мокирует ответ `ai-service` и проверяет, что `DataResolverService` вызывается с правильными аргументами.

## Критерии приемки

- Сквозной поток "Planner -> Resolver" полностью реализован в `orchestra-api`.
- API `POST /api/v1/ai/data/generate` возвращает данные, обработанные `DataResolverService`, которые являются валидными для тестового окружения.

