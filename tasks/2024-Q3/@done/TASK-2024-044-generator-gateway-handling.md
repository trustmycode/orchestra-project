---
id: TASK-2024-044
title: "Задача 4.4.3 (Backend): Обработка шлюзов (Exclusive & Parallel) и синхронизация"
status: done
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2025-11-22
parents: [TASK-2024-031]
dependencies: [TASK-2024-043, TASK-2024-079]
arch_refs: [ADR-0010, ADR-0021]
audit_log:
  - {date: 2025-11-22, user: "@RoboticArchitect", action: "updated to include BARRIER step requirement"}
---
## Описание

Расширить алгоритм обхода графа для корректной обработки `Exclusive` и `Parallel` шлюзов. Критически важно обеспечить синхронизацию параллельных веток с помощью шага `BARRIER`, как предписано в `ADR-0021`.

## Ключевые шаги

1. **Exclusive Gateway:** При достижении шлюза алгоритм должен рекурсивно разветвляться, создавая новые пути (отдельные `TestScenario`) для каждой исходящей ветки.

2. **Parallel Gateway (Fork):** При достижении разветвления алгоритм должен начать отслеживать несколько параллельных потоков выполнения.

3. **Parallel Gateway (Join):** При схождении веток необходимо вставить в сценарий шаг типа `BARRIER`.

   - В метаданные шага `BARRIER` (`trackedSteps`) добавить алиасы последних шагов каждой сходящейся ветки.

   - Это гарантирует, что Execution Engine дождется выполнения всех веток перед продолжением.

## Критерии приемки

- Для BPMN с `Exclusive Gateway` создается несколько `TestScenario`, по одному на каждую ветку.

- Для BPMN с `Parallel Gateway` создается один базовый `TestScenario`.

- В сценарии с параллельными ветками присутствует шаг `BARRIER`, сконфигурированный на ожидание завершения веток.
