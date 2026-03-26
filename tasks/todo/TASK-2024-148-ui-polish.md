---
id: TASK-2024-148
title: "Задача 5.2.3 (Frontend): Визуальная полировка, отступы и Empty States"
status: todo
priority: medium
type: refactoring
estimate: 6h
created: 2025-11-22
parents: [TASK-2024-145]
---

## Описание

Устранить визуальные дефекты ("слипание" элементов) и внедрить стандартизированные заглушки для пустых списков.

## Ключевые шаги

1.  **Компонент `EmptyState`:**

    *   Создать `src/components/ui/empty-state.tsx`.

    *   Принимает: `icon`, `title`, `description`, `action` (ReactNode).

    *   Стили: Центрирование по вертикали и горизонтали, серый текст, иконка большого размера.

2.  **Внедрение EmptyState:**

    *   Применить в `ProcessListView`, `DataSetListView`, `SuiteRunListView`, `TestRunListView`.

3.  **Исправление отступов (Spacing):**

    *   В `AiDataGenerationModal.tsx` и `ImportView.tsx`:

        *   Увеличить вертикальные отступы между полями (`space-y-4` или `gap-4`).

        *   Отделить блок кнопок (Footer) от контента: добавить `mt-6`, `pt-4` и `border-t`.

    *   В `ScenarioBuilderView.tsx`: Проверить отступы между кнопками удаления шагов и полями ввода.

## Критерии приемки

- [ ] На пустых страницах отображается красивая заглушка по центру.

- [ ] В модальных окнах кнопки "Save/Cancel" визуально отделены от полей ввода.

- [ ] Текст в формах не прилипает к границам блоков.

