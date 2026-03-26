---
id: TASK-2024-140
title: "Задача 4.10.14 (Full Stack): Поддержка Custom Instructions для генерации на уровне Suite"
status: todo
priority: high
type: feature
estimate: 4h
created: 2025-11-21
parents: [TASK-2024-037]
dependencies: [TASK-2024-131, TASK-2024-134]
---

## Описание
В текущем UI и API отсутствует возможность передавать пользовательские инструкции (`custom instructions`) при генерации данных для всего `ScenarioSuite`. Это ограничивает пользователя, не позволяя ему задать высокоуровневый контекст для всего набора тестов (например, "протестировать для VIP-клиента" или "использовать данные для региона EMEA").

Необходимо добавить эту возможность на всех уровнях: UI, API и в логику AI-агента.

## Техническое решение (Backend)

1.  **API Change:**
    *   Обновить DTO для `POST /api/v1/ai/jobs/generate`, добавив опциональное поле `String instructions`.

2.  **Logic Change (`SuiteAnalysisService` из TASK-2024-137):**
    *   В **"Step 2: Suite Linking (Reduce)"** передавать полученные `instructions` в промпт для "AI-Архитектора".
    *   **Пример модификации промпта:**
        *   *Было:* "Analyze these scenarios and find shared variables."
        *   *Стало:* "Analyze these scenarios to find shared variables. **Keep in mind the user's primary goal: '{instructions}'**. This goal should guide your decisions on which variables are most important to link."

## Техническое решение (Frontend)

1.  **UI Change (`AiDataGenerationModal.tsx`):**
    *   Убедиться, что поле "Custom Instructions (Optional)" **остается видимым**, когда пользователь переключает `Target Scope` на `Suite Context`.
    *   Логика компонента уже должна поддерживать это, но нужно проверить и, при необходимости, исправить.

2.  **API Call Change:**
    *   При вызове `startGenerationJob(...)` передавать содержимое `textarea` в поле `instructions` тела запроса.

## Критерии приемки
- [ ] UI позволяет ввести инструкции для `Suite Context`.
- [ ] API принимает `instructions` и передает их в `ai-service`.
- [ ] Сквозной тест: при генерации для сьюта с инструкцией "используй данные для пользователя с именем 'admin_vasya'" в итоговом JSON для всех релевантных полей будет подставлен UUID именно этого пользователя.