---
id: TASK-2024-044
title: "Задача 4.4.3 (Backend): Обработка шлюзов (Exclusive & Parallel)"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-031]
dependencies: [TASK-2024-043]
arch_refs: [ADR-0010, ADR-0021]
---
## Описание
Расширить алгоритм обхода графа для корректной обработки `Exclusive` и `Parallel` шлюзов.

## Ключевые шаги
1. **Exclusive Gateway:** При достижении шлюза алгоритм должен рекурсивно разветвляться, создавая новые пути для каждой исходящей ветки.
2. **Parallel Gateway:** При достижении шлюза алгоритм должен последовательно обойти все исходящие ветки и объединить их в один путь.

## Критерии приемки
- Для BPMN с `Exclusive Gateway` создается несколько `TestScenario`, по одному на каждую ветку.
- Для BPMN с `Parallel Gateway` создается один `TestScenario`, включающий шаги из всех параллельных веток.
