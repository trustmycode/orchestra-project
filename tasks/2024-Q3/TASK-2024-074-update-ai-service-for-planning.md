---
id: TASK-2024-074
title: "Задача 4.10.2 (Backend): Реализация DataPlannerAgent на Spring AI"
status: backlog
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-072]
dependencies: [TASK-2024-035, TASK-2024-097, TASK-2024-098, TASK-2024-116]
arch_refs: [ADR-0019, ADR-0031]
---
## Описание
Реализовать логику агента-планировщика (`DataPlannerAgent`) внутри `ai-service`, используя чистый Java-стек. Агент должен анализировать входной контекст и генерировать `DataPlan`.

## Ключевые шаги
1.  Создать класс `DataPlannerAgent`, реализующий интерфейс `AiAgent`.
2.  Подключить инструменты из `AgentToolbox` (получение схемы API, справочников).
3.  Загрузить промпт `data_planner_system_v1` из БД.
4.  Реализовать цикл:
    - Сформировать контекст.
    - Вызвать LLM.
    - Если LLM просит вызвать Tool -> вызвать Java-метод -> вернуть результат в LLM.
    - Получить финальный `DataPlan`.

## Критерии приемки
-   Агент реализован на Java.
-   Генерация плана данных проходит успешно с использованием модели `gpt-oss:20b`.
