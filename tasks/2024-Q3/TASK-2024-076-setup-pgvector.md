---
id: TASK-2024-076
title: "Задача 4.10.4 (Infra/Backend): Настройка PGVector и индексатора"
status: backlog
priority: medium
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-072]
arch_refs: [ADR-0019]
---
## Описание
Добавить поддержку векторного поиска в PostgreSQL (PGVector) и подготовить данные для семантических запросов `Data Resolver`.

## Ключевые шаги
1. Обновить Postgres в `docker-compose.yml`, включив расширение PGVector.
2. Создать миграции с `CREATE EXTENSION vector;` и нужными колонками `vector`.
3. Реализовать `DataIndexer`, который создаёт эмбеддинги для записей и заполняет векторные колонки.
4. Настроить `VectorStore`/репозиторий для гибридных запросов.

## Критерии приемки
- В БД доступны векторные колонки и индекс.
- `Data Resolver` может выпонять запросы с использованием `embedding <-> query`.
