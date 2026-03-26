---
id: TASK-2024-146
title: "Задача 5.2.1 (Frontend): Исправление Layout и изоляция скролла"
status: todo
priority: critical
type: refactoring
estimate: 8h
created: 2025-11-22
parents: [TASK-2024-145]
---

## Описание

Необходимо изменить структуру `MainLayout.tsx`, чтобы предотвратить уход бокового меню вверх при прокрутке длинного контента. Также нужно устранить горизонтальный скролл всей страницы (`body`), который возникает из-за широких таблиц или JSON-блоков.

## Ключевые шаги

1.  **Рефакторинг `MainLayout.tsx`:**

    *   Установить на корневой контейнер: `h-screen w-full overflow-hidden flex`.

    *   Для `aside` (Sidebar): убрать `min-h-screen`, добавить `h-full overflow-y-auto`.

    *   Для правой части (Header + Content): `flex flex-col h-full flex-1`.

    *   Для `main` (Content): `flex-1 overflow-y-auto overflow-x-hidden`.

2.  **Изоляция таблиц:**

    *   В `components/ui/table.tsx` обернуть таблицу в `div` с классом `w-full overflow-x-auto`.

3.  **Изоляция блоков кода:**

    *   В `StepResultDetails.tsx` (и других местах вывода JSON) добавить к тегу `<pre>` классы `whitespace-pre-wrap word-break-all` (для переноса) либо `overflow-x-auto` (для локального скролла).

## Критерии приемки

- [ ] При прокрутке длинного списка сценариев левое меню и шапка остаются на месте.

- [ ] При наличии широкой таблицы скроллбар появляется только у таблицы, а не у всего окна браузера.

- [ ] Мобильная версия (если используется) не сломана.


