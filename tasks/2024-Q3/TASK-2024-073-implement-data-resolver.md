---
id: TASK-2024-073
title: "Задача 4.10.1 (Backend): Реализация компонента `Data Resolver`"
status: backlog
priority: high
type: task
estimate: 16h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-072]
arch_refs: [ADR-0019]
---
## Описание
Создать в `orchestra-api` сервис `DataResolverService`, который по критериям из `DataPlan` подбирает реальные тестовые данные.

## Ключевые шаги
1. Спроектировать интерфейс `DataResolver` и базовые реализации (CustomerResolver, ProductResolver и т.д.).
2. Настроить получение соединений/ORM для запросов к тестовой БД (используя `Environment`).
3. Реализовать маппинг критериев (`segment`, `inStock`) в SQL-запросы и сборку ответа.
4. Добавить аннотации/реестр, связывающий поля API с конкретными резолверами.

## Критерии приемки
- `DataResolverService` по входному `DataPlan` возвращает набор сущностей с реальными идентификаторами.
