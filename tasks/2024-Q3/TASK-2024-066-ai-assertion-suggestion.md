---
id: TASK-2024-066
title: "Задача 4.9 (AI): Агент для предложения проверок (Assertion Agent)"
status: backlog
priority: low
type: feature
estimate: 16h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-027]
dependencies: [TASK-2024-097, TASK-2024-098, TASK-2024-073]
arch_refs: [ADR-0025]
---
## Описание
Реализовать агента, который предлагает `ASSERTION`-шаги.

## Ключевые шаги
1.  Добавить инструмент `getKnownSideEffects(operationId)`, который может обращаться к базе знаний или анализировать имя операции (например, `create` -> `DB INSERT`).
2.  Агент формирует список рекомендованных шагов (DB, Kafka) с предзаполненными параметрами.

## Критерии приемки
-   UI предлагает добавить релевантные проверки после ACTION-шага.
