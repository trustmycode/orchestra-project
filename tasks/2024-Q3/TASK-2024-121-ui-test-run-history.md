---
id: TASK-2024-121
title: "Задача 5.2: Реализация истории запусков (Backend & UI)"
status: done
priority: high
type: feature
estimate: 6h
created: 2025-11-21
parents: [TASK-2024-108]
---

## Описание

Пользователи не видят историю запусков. Необходимо реализовать API и UI для просмотра списка всех TestRuns.

## Ключевые шаги

1.  **Backend:** Добавить метод `findAll` в `TestRunService` (сортировка по `createdAt DESC`).

2.  **Backend:** Добавить эндпоинт `GET /api/v1/testruns` в `TestRunController`.

3.  **Frontend:** Создать компонент `TestRunListView`.

4.  **Frontend:** Добавить ссылку "Test Runs" в сайдбар (`MainLayout`).

5.  **Frontend:** Зарегистрировать роут `/runs` в `App.tsx`.

## Критерии приемки

-   В сайдбаре есть пункт "Test Runs".
-   Открывается таблица с историей запусков, отсортированная от новых к старым.
-   Можно кликнуть на строку и перейти к деталям запуска.

