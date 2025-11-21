---
id: TASK-2024-116
title: "Задача 2.2.6: Реализация управления промптами в БД (Prompt Engine)"
status: in progress
priority: high
type: feature
estimate: 10h
created: 2025-11-20
parents: [TASK-2024-033]
arch_refs: [ADR-0032]
---
## Описание
Реализовать механизм хранения, кэширования и извлечения промптов из базы данных PostgreSQL. Это позволит менять поведение агентов без пересборки приложения.

## Ключевые шаги
1.  **DB Schema:** Создать таблицу `ai_prompts` (id, key, template, version, updated_at).
2.  **Entity/Repo:** Создать JPA сущность `PromptTemplateEntity` и репозиторий.
3.  **Service:** Реализовать `PromptManagerService`:
    -   Метод `getPrompt(String key)` с кэшированием (использовать Caffeine Cache).
    -   Метод `updatePrompt(String key, String newTemplate)` со сбросом кэша.
4.  **Bootstrap:** Добавить Liquibase миграцию с начальными промптами (System Prompts для Planner, Analyst).

## Критерии приемки
-   При старте приложения промпты доступны.
-   Изменение записи в БД (и сброс кэша) мгновенно влияет на ответы агента.
-   Высокая производительность (чтение из памяти).
