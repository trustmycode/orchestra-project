---
id: TASK-2024-098
title: "Задача 4.10.1.5 (Backend): Реализация ToolRegistry и Java-инструментов"
status: done
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-072]
dependencies: [TASK-2024-097]
arch_refs: [ADR-0031]
---
## Описание
Создать реестр инструментов (`ToolRegistry`) на базе Spring AI Function Calling API. Инструменты должны быть обычными Java-бинами.

## Ключевые шаги
1.  Создать сервис `ToolRegistry`.
2.  Реализовать базовые инструменты как `@Bean` с описанием `FunctionCallback`:
    -   `SchemaLookupTool`: получение схемы OpenAPI по ID сервиса.
    -   `DictionaryLookupTool`: поиск значений в справочниках.
3.  Обеспечить механизм автоматической регистрации инструментов в `ChatClient` при запуске агента.

## Критерии приемки
-   Созданы Java-бины для инструментов.
-   Агент может вызвать инструмент (LLM возвращает function call, Java исполняет его).
