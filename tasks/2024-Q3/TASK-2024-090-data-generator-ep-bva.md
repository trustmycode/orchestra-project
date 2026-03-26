---
id: TASK-2024-090
title: "Задача 4.4.8 (Backend): Генерация критериев данных (EP/BVA) для Data Resolver"
status: backlog
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2025-11-22
parents: [TASK-2024-031]
dependencies: [TASK-2024-036, TASK-2024-089, TASK-2024-073]
arch_refs: [ADR-0019]
audit_log:
  - {date: 2025-11-22, user: "@RoboticArchitect", action: "aligned with Planner-Resolver pattern"}
---
## Описание

Расширить возможности генерации сценариев, внедряя техники Equivalence Partitioning (EP) и Boundary Value Analysis (BVA). Важно: генератор должен создавать не "сырые" данные, а **Data Plans** (критерии), которые затем будут исполнены `Data Resolver`'ом для получения валидных ID и ссылочной целостности.

## Ключевые шаги

1.  На основе `Path Predicate` (полученного в `TASK-2024-089`) вычислить граничные значения (например, для `amount > 1000` это `1000`, `1001`, `999`).

2.  Сформировать `TestDataSet` с типом `AI_GENERATED`, где в поле `data` лежат не просто числа, а инструкции для резолвера.

    - *Пример:* `{ "amount": 1001, "customerId": { "semanticCriteria": "customer with active account" } }`.

3.  Обеспечить, чтобы для одного логического пути создавалось несколько вариаций `TestScenario` (или один сценарий с несколькими `TestDataSet`), покрывающих границы.

## Критерии приемки

-   Для условия `x > N` создаются наборы данных для `N`, `N+1`, `N-1`.

-   Сгенерированные наборы данных совместимы с `DataResolverService` (содержат семантические критерии для связанных сущностей).

-   Соблюдается принцип: генератор сценариев задает *границы*, а резолвер находит *реальные данные*, удовлетворяющие этим границам.
