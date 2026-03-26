---
id: TASK-2024-158
title: "Задача: Локализация AI Wizard и модальных окон"
status: todo
priority: high
type: refactoring
estimate: 8h
created: 2025-11-22
parents: [TASK-2024-108]
dependencies: [TASK-2024-156]
---

## Описание

Перевести интерфейсы взаимодействия с AI, так как они содержат много инструкций и пояснений.

## Ключевые шаги

1.  **`translation.json`**: Добавить секции `aiWizard`, `generateModal`, `suggestions`.

2.  **`AiWizard.tsx`**:

    *   Перевести шаги степпера ("Source", "Coverage", "Mapping").

    *   Перевести описания стратегий генерации ("Happy Path Only", "Full Coverage").

3.  **`AiDataGenerationModal.tsx`**:

    *   Перевести лейблы ("Target Scope", "Context Reference").

    *   Перевести плейсхолдеры для Custom Instructions (например, "Например: используй данные для VIP клиентов...").

4.  **`SuggestionCard.tsx`**:

    *   Перевести кнопки действий ("Dismiss" -> "Скрыть", "Apply" -> "Применить").

## Критерии приемки

- [ ] Мастер генерации сценариев и окно генерации данных полностью на русском языке.

- [ ] Плейсхолдеры подсказывают пользователю вводить инструкции на русском.

