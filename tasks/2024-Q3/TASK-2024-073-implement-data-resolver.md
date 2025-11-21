---
id: TASK-2024-073
title: "Задача 4.10.1 (Backend): Реализация компонента `Data Resolver` с поддержкой RAG"
status: done
priority: high
type: task
estimate: 16h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-072]
dependencies: [TASK-2024-076]
arch_refs: [ADR-0019]
---
## Описание
Создать сервис `DataResolverService`, который исполняет `DataPlan`. Он должен поддерживать как точные критерии (SQL `WHERE`), так и семантические (Vector Search).

## Ключевые шаги
1.  Внедрить `VectorStore` в `DataResolverService`.
2.  Реализовать логику:
    - Если в плане есть поле `semanticCriteria` (например, "похожий на..."), выполнять `vectorStore.similaritySearch()`.
    - Полученные ID записей использовать для фильтрации в основном SQL-запросе.
3.  Реализовать "Индексатор" (фоновый процесс или по событию), который при создании/обновлении тестовых данных обновляет их эмбеддинги в `vector_store`.

## Критерии приемки
-   Резолвер корректно находит данные по смысловому описанию (RAG сценарий).
