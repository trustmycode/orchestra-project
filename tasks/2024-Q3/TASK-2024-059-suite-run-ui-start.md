---
id: TASK-2024-059
title: "Задача 4.6.3 (Frontend): UI для запуска SuiteRun"
status: backlog
priority: medium
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-056]
dependencies: [TASK-2024-057]
arch_refs: []
---
## Описание
Реализовать в UI возможность запуска `ScenarioSuite`.

## Ключевые шаги
1. На странице `ScenarioSuiteDetailView` добавить кнопку "Run Suite".
2. Создать модальное окно для настройки параметров запуска (выбор `Environment`, `runMode` и т.д.).
3. Реализовать вызов API `POST /suite-runs`.
4. После успешного запуска перенаправлять пользователя на страницу отчета `SuiteRun`.

## Критерии приемки
- Пользователь может запустить `ScenarioSuite` из UI.
