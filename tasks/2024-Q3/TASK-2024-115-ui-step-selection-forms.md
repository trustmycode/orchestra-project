---
id: TASK-2024-115
title: "Задача 5.1.6 (Frontend): Реализация меню выбора типа шага и динамических форм"
status: done
priority: high
type: feature
estimate: 16h
created: 2024-11-20
parents: [TASK-2024-108]
dependencies: [TASK-2024-110]
---
## Описание
Улучшить UX конструктора сценариев (`ScenarioBuilderView`), заменив базовую кнопку добавления шага на меню выбора типа. Для каждого типа шага (HTTP, DB, Kafka, Barrier) реализовать специализированную форму настройки, скрывающую сложность редактирования raw JSON структур.

## Ключевые шаги
1.  **Меню выбора:**
    *   Заменить кнопку "+ Add Step" на компонент `DropdownMenu` (Shadcn UI).
    *   Добавить пункты: "HTTP Request", "DB Assertion", "Kafka Assertion", "Barrier".
    *   При выборе пункта создавать шаг с корректно предустановленными `kind`, `channelType` и структурой `meta`.
2.  **Динамические формы:**
    *   Создать отдельные React-компоненты для рендеринга настроек в зависимости от типа шага:
        *   `HttpStepForm`: Method, URL, Headers, Body editor.
        *   `DbAssertionForm`: Data Source Alias (выбор из Environment), SQL Query, Polling Interval, Timeout.
        *   `KafkaAssertionForm`: Cluster Alias, Topic, Key/Value Matchers.
    *   Реализовать валидацию полей формы перед сохранением в JSON-структуру шага.
3.  **Режим эксперта:**
    *   Сохранить возможность прямого редактирования JSON (`action`, `expectations`, `endpointRef`) через переключатель "Advanced Mode" или "Raw JSON View".

## Критерии приемки
-   Пользователь добавляет шаг конкретного типа (например, DB Assertion) через меню, и шаг создается с правильным типом `DB` и `ASSERTION`.
-   Пользователь заполняет поля "SQL" и "Data Source" в удобной форме, и эти данные корректно маппятся в `action.meta` JSON.
-   JSON-редакторы по умолчанию скрыты или занимают меньше места, уступая место типизированным полям.
