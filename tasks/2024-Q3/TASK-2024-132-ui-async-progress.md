---
id: TASK-2024-132
title: "Задача 5.1.7 (Frontend): UI с прогресс-баром для генерации данных"
status: todo
priority: medium
type: feature
estimate: 6h
created: 2025-11-21
parents: [TASK-2024-108]
dependencies: [TASK-2024-131]
---

## Описание
Адаптировать модальное окно `AiDataGenerationModal` для работы с асинхронным API.

## Ключевые шаги

1.  **Логика вызова:**
    *   Вместо `await generateData(...)` делать `startGenerationJob(...)`.
    *   Получив `jobId`, переходить в режим "Polling".

2.  **Интерфейс:**
    *   Вместо спиннера на кнопке "Generate" показывать **Progress Bar** (компонент `Progress` из Shadcn).
    *   Отображать текстовый статус: "Generating data for scenario 3 of 10...".

3.  **Завершение:**
    *   Когда статус `COMPLETED`, забрать JSON и вставить в редактор (как раньше).
    *   Если `FAILED`, показать ошибку.

## Критерии приемки
- [ ] Пользователь видит прогресс выполнения длительной операции.
- [ ] Интерфейс не блокируется "намертво".