---
id: TASK-2024-155
title: "Задача: Локализация системных промптов AI (Russian Language)"
status: todo
priority: high
type: database
estimate: 4h
created: 2025-11-22
parents: [TASK-2024-033]
arch_refs: [ADR-0032]
---

## Описание

Необходимо обновить системные промпты в базе данных, чтобы `ai-service` генерировал контент (описания, имена, аналитику) на русском языке. При этом критически важно сохранить техническую структуру (ключи JSON) на английском для корректной работы парсеров.

## Ключевые шаги

1.  Создать файл миграции Flyway `services/orchestra-api/src/main/resources/db/migration/V23__Localize_prompts_to_russian.sql`.

2.  Написать SQL-запросы `UPDATE` для таблицы `ai_prompts` для следующих ключей:

    *   **`data_planner_system_v1`**: Добавить инструкцию: *"Generate string values (names, descriptions, cities) in Russian. JSON keys MUST remain in English (camelCase)."*

    *   **`analyst_system_prompt`**: Изменить роль на "QA-аналитик" и потребовать рекомендации на русском языке.

    *   **`scenario_analyst_system_v1`**: Потребовать описание переменных на русском.

    *   **`suite_linker_system_v1`**: Потребовать обоснование ("reasoning") на русском.

    *   **`mapping_agent_system_v1`**: Потребовать обоснование выбора эндпоинта на русском.

## Критерии приемки

- [ ] Миграция успешно применяется при запуске `orchestra-api`.

- [ ] При генерации данных AI возвращает JSON вида `{"firstName": "Иван", "city": "Москва"}`, а не `{"firstName": "Ivan", "city": "Moscow"}`.

- [ ] Отчеты об ошибках (`ReportRecommendations`) приходят на русском языке.

