---
id: TASK-2024-055
title: "Задача 3.2.3 (Backend): Реализация логики polling и валидации для DB-шагов"
status: completed
priority: medium
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-023]
dependencies: [TASK-2024-054]
arch_refs: [ARCH-protocol-plugins]
audit_log:
  - {date: 2024-07-30, user: "@AI-Developer", action: "implemented polling and validation logic"}
---
## Описание
Реализовать в `DbProtocolPlugin` основную логику выполнения `ASSERTION`: polling SQL-запроса и валидацию результата.

## Ключевые шаги
1. Реализовать цикл `polling` с использованием `timeoutMs` и `pollIntervalMs` из метаданных шага.
2. Внутри цикла выполнять SQL-запрос (`action.meta.sql`) с помощью `JdbcTemplate`.
3. Сравнивать полученный результат с правилами, описанными в `expectations.businessRules` (например, "количество записей > 0", "поле status == 'COMPLETED'").
4. При успехе — завершать шаг со статусом `PASSED`. При таймауте — `FAILED`.

## Критерии приемки
- Шаг "Проверка в БД" успешно ожидает появления нужных данных и завершается со статусом `PASSED`.
- Если данные не появляются в течение таймаута, шаг завершается со статусом `FAILED`.
