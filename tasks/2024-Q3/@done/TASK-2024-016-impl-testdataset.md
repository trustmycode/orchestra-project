---
id: TASK-2024-016
title: "Задача 2.1 (Backend/Frontend): Реализация TestDataSet"
status: backlog
priority: medium
type: feature
estimate: 16h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-015]
arch_refs: [ARCH-data-management, ARCH-data-model]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать функциональность управления наборами тестовых данных (`TestDataSet`).

## Критерии приемки
- (Backend) Реализованы CRUD API для `TestDataSet`.
- (Backend) `Execution Engine` поддерживает параметризацию шагов с использованием данных из `TestDataSet` (например, `{{data.field}}`).
- (Frontend) Создан UI для создания, редактирования и просмотра `TestDataSet`.
- (Frontend) При запуске теста пользователь может выбрать `TestDataSet`.
