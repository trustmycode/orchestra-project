---
id: TASK-2024-112
title: "Задача 5.1.4 (Frontend): Интерфейсы для AI-функций (Wizard & Suggestions)"
status: backlog
priority: medium
type: feature
estimate: 16h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-108]
dependencies: [TASK-2024-110, TASK-2024-114]
---
## Описание
Создать UI-компоненты для взаимодействия с AI, следуя дизайн-документу "Magic Orchestra".

## Ключевые шаги
1.  **AI Wizard:** Реализовать модальное окно (`Dialog`) со `Stepper`, включающее:
    - Шаг 1 — Выбор источника (BPMN/Sequence).
    - Шаг 2 — Настройки покрытия.
    - Шаг 3 — Маппинг сервисов: получать список участников процесса через `GET /processes/{id}/participants` и отображать форму сопоставления с OpenAPI specs.
2.  **AI Suggestions:** Создать компонент `SuggestionCard` (фон `bg-violet-50 dark:bg-violet-950/20`, иконка ✨) с кнопками "Apply"/"Dismiss" и внедрить его в конструктор сценариев.
3.  **Data Resolver UI:** Создать интерфейс для настройки `Data Resolver` с возможностью выбора источников данных и сопоставления полей.

## Критерии приемки
-   Wizard позволяет выбрать процесс, настроить маппинг и запустить генерацию сценария.
-   Пользователь видит подсказки AI в виде `SuggestionCard` и может применять/отклонять их.
-   Раздел настроек Data Resolver соответствует визуальной теме Shadcn/Tailwind и поддерживает тёмный режим.
