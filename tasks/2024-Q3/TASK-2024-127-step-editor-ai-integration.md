```markdown

---

id: TASK-2024-127

title: "Задача: Интеграция генерации данных в редактор шагов"

status: done

priority: high

type: feature

estimate: 6h

created: 2024-07-31

updated: 2024-07-31

parents: [TASK-2024-037]

dependencies: [TASK-2024-126]

arch_refs: [ADR-0019, ADR-0030]

audit_log:

  - {date: 2024-07-31, user: "@RoboticArchitect", action: "created"}
  - {date: 2024-07-31, user: "@RoboticAI", action: "implemented"}

---

## Описание

Внедрить функционал вызова AI-генерации непосредственно в компоненты редактирования шагов сценария (`StepEditors.tsx`). Пользователь должен иметь возможность нажать кнопку "Generate with AI" при редактировании JSON-тела запроса или SQL-запроса, чтобы получить контекстно-валидные данные.

## Ключевые шаги

1.  **Прокидывание контекста (`ScenarioBuilderView.tsx` -> `StepEditors.tsx`):**

    *   В `ScenarioBuilderView` передать `scenario.id` (если сценарий сохранен) и `selectedEnvId` в пропсы компонента `StepEditor`.

    *   Обновить интерфейс `StepEditorProps` в `StepEditors.tsx`, добавив поля `scenarioId` и `environmentId`.

2.  **UI Кнопка генерации (`StepEditors.tsx`):**

    *   В компоненты `HttpStepForm` (для поля Body) и `DbAssertionForm` (для поля SQL) добавить кнопку с иконкой `Sparkles` (из `lucide-react`).

    *   Кнопка должна располагаться над соответствующим `textarea`.

    *   **Важно:** Кнопка должна быть `disabled`, если `scenarioId` или `step.id` отсутствуют (сценарий/шаг не сохранены), с подсказкой "Save scenario first".

3.  **Логика вызова:**

    *   При клике вызывать метод `generateAiData` (из TASK-2024-126).

    *   Передавать: `scenarioId`, `stepId`, `environmentId` и `mode: 'HAPPY_PATH'`.

    *   Отображать состояние загрузки (спиннер на кнопке).

4.  **Обработка ответа:**

    *   При успехе: автоматически вставлять полученный JSON (или SQL фрагмент) в `textarea`.

    *   Если в ответе есть поле `notes`, отображать его пользователю (например, через `toast` или текстовый блок под редактором).

    *   При ошибке: выводить понятное сообщение (например, "AI service unavailable" или "Environment not selected").

## Критерии приемки

*   В редакторе HTTP шага над полем Body есть кнопка "Generate with AI".

*   В редакторе DB шага над полем SQL есть кнопка "Generate with AI".

*   Кнопка активна только для сохраненных шагов.

*   Нажатие кнопки инициирует запрос к бэкенду с корректными ID.

*   Полученные данные подставляются в форму.

```

