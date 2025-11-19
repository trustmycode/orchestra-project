---
id: TASK-2024-106
title: "Задача 4.14.2 (Backend): Конвертер PlantUML в ControlFlowGraph"
status: backlog
priority: high
type: task
estimate: 12h
created: 2024-07-30
parents: [TASK-2024-104]
dependencies: [TASK-2024-105]
arch_refs: [ADR-0029]
---
## Описание
Реализовать логику преобразования AST PlantUML в универсальный граф потока управления (`controlFlowGraph`), используемый в `ProcessVersion`.

## Ключевые шаги
1.  Маппинг сообщений (`->`) в узлы типа `Task`.
2.  Маппинг блоков `alt/opt` в структуру `Exclusive Gateway`.
3.  Маппинг блоков `par` в структуру `Parallel Gateway`.
4.  Маппинг блоков `loop` в циклические связи в графе.
5.  Интеграция с `ImportService`: добавление метода `importPuml`, который использует парсер и конвертер.

## Критерии приемки
-   Загруженный PUML-файл сохраняется в БД как `ProcessVersion` с корректно заполненным `controlFlowGraph`.

