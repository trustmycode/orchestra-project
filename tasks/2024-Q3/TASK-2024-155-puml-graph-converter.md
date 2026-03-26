---
id: TASK-2024-155
title: "Задача 4.14.4 (Backend): Конвертация PlantUML в ControlFlowGraph"
status: todo
priority: high
type: feature
estimate: 12h
created: 2025-11-22
parents: [TASK-2024-104]
arch_refs: [ADR-0029]
---

## Описание

Чтобы использовать общий механизм генерации сценариев и данных ("Ready to Run"), необходимо научить систему превращать текстовое описание Sequence-диаграммы в универсальный `ControlFlowGraph`.

## Ключевые шаги

1.  **Parser Service:**

    *   Реализовать парсинг текстового формата `.puml`.

    *   Извлекать участников (`Participant`) и сообщения (`Message`).

    *   Распознавать блоки управления: `alt/else` (Exclusive Gateway), `opt` (Exclusive Gateway с пустой веткой), `par` (Parallel Gateway), `loop` (Cyclic Edge).

2.  **Graph Builder:**

    *   Преобразовать сообщения вида `Client -> API: Create Order` в узлы графа типа `ACTION`.

    *   Имя узла (`name`) = Текст сообщения ("Create Order").

    *   Метаданные узла (`metadata`) = Сохранить имена отправителя и получателя (`source: Client`, `target: API`). Это критично для маппинга.

3.  **Edge Cases:**

    *   Корректно обрабатывать вложенные блоки (например, `alt` внутри `loop`).

## Критерии приемки

-   На вход подается строка PlantUML, на выходе — валидный объект `ControlFlowGraph`.

-   В метаданных узлов сохранены имена участников взаимодействия.


