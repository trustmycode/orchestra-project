---
id: TASK-2024-109
title: "Задача 5.1.1 (Frontend): Фундамент (Стек, Роутинг, Тема)"
status: backlog
priority: high
type: chore
estimate: 16h
created: 2024-07-30
parents: [TASK-2024-108]
arch_refs: [ADR-0030]
---
## Описание
Настроить базовый стек технологий и структуру приложения.

## Ключевые шаги
1.  Установить и настроить Tailwind CSS и Shadcn/ui.
2.  Настроить React Router DOM и создать `MainLayout` (Shadcn Shell) с постоянным сайдбаром, хлебными крошками (`Breadcrumbs`) и area для `Outlet`.
3.  Реализовать переключение темной/светлой темы и вынести `ThemeToggle` в header согласно дизайн-гайду "Magic Orchestra".

## Критерии приемки
-   Приложение запускается с новым Layout.
-   Работает навигация по URL, включая отображение Breadcrumbs для вложенных роутов.
-   Переключатель темы меняет цветовую схему.
