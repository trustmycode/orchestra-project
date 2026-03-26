---
id: TASK-2024-156
title: "Задача 4.14.5 (Backend): Интеграция PlantUML в асинхронный генератор"
status: todo
priority: high
type: feature
estimate: 4h
created: 2025-11-22
parents: [TASK-2024-104]
dependencies: [TASK-2024-150, TASK-2024-155]
---

## Описание

Подключить парсер PlantUML к основному сервису генерации `ProcessToScenarioGenerator`.

## Ключевые шаги

1.  **Routing Logic:**

    *   В методе `generateAsync` (см. TASK-2024-150) добавить проверку `sourceType`.

    *   Если `BPMN` -> использовать `BpmnGraphParser`.

    *   Если `PLANTUML` -> использовать новый `PumlGraphConverter`.

2.  **Validation:**

    *   Если граф не удалось построить из PUML (синтаксическая ошибка), корректно завершать `AiJob` со статусом `FAILED` и понятным сообщением.

## Критерии приемки

-   Эндпоинт `/async` принимает ID процесса типа `PLANTUML`.

-   Генерация проходит успешно, создается `ScenarioSuite` со статусом `READY`.


