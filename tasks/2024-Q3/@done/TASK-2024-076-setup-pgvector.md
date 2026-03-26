---
id: TASK-2024-076
title: "Задача 4.10.4 (Infra/Backend): Настройка PGVector и Spring AI VectorStore"
status: done
priority: medium
type: task
estimate: 8h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-072]
arch_refs: [ADR-0019, ADR-0031]
---
## Описание
Настроить поддержку векторного поиска в PostgreSQL и интегрировать её с приложением через абстракцию `VectorStore` из Spring AI.

## Ключевые шаги
1.  **Infra:** Убедиться, что Docker-образ Postgres имеет расширение `pgvector` (использовать образ `pgvector/pgvector:pg16`).
2.  **Migration:** Создать Liquibase миграцию:
    ```sql
    CREATE EXTENSION IF NOT EXISTS vector;
    -- Создание таблицы vector_store, требуемой Spring AI
    CREATE TABLE vector_store (
        id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
        content text,
        metadata json,
        embedding vector(1024) -- размерность зависит от модели mxbai-embed-large
    );
    CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
    ```
3.  **App Config:** В `ai-service` (или `orchestra-api`, где живет Resolver) подключить `spring-ai-pgvector-store-spring-boot-starter`.
4.  **Embedding Client:** Настроить `OllamaEmbeddingClient` на использование модели `mxbai-embed-large`.

## Критерии приемки
-   Приложение успешно стартует и создает бин `VectorStore`.
-   Можно сохранить текст в векторную БД и найти его через `vectorStore.similaritySearch()`.
