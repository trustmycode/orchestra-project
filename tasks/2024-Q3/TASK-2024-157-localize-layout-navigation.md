---
id: TASK-2024-157
title: "Задача: Локализация Layout и навигации"
status: todo
priority: medium
type: refactoring
estimate: 6h
created: 2025-11-22
parents: [TASK-2024-108]
dependencies: [TASK-2024-156]
---

## Описание

Перевести на русский язык основные элементы навигации и оболочки приложения.

## Ключевые шаги

1.  **`translation.json`**: Добавить секции `sidebar`, `header`, `common`.

2.  **`MainLayout.tsx`**:

    *   Заменить названия пунктов меню (Dashboard, Processes, Specs и т.д.) на ключи `t('sidebar.processes')`.

    *   Перевести заголовки групп ("Menu", "AI Tools", "Settings").

3.  **`ImportView.tsx`**:

    *   Перевести заголовки карточек импорта (BPMN, OpenAPI, PlantUML) и описания.

    *   Перевести кнопки и лейблы форм.

## Критерии приемки

- [ ] Боковое меню и заголовки страниц отображаются на русском языке.


