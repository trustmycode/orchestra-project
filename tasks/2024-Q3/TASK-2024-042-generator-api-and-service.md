---
id: TASK-2024-042
title: "Задача 4.4.1 (Backend): API и сервис для генератора сценариев"
status: backlog
priority: high
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-031]
arch_refs: []
---
## Описание
Создать API-эндпоинт `POST /scenario-suites/from-process` и скелет сервиса `ProcessToScenarioGenerator`.

## Ключевые шаги
1. Реализовать `ScenarioSuiteController` с эндпоинтом, принимающим `ScenarioSuiteGenerateRequest`.
2. Создать класс `ProcessToScenarioGenerator` с методом `generate(request)`.
3. Настроить базовую логику: загрузка `ProcessVersion`, создание родительского `ScenarioSuite`.

## Критерии приемки
- Эндпоинт `POST /scenario-suites/from-process` доступен и вызывает сервис генерации.
- Создается пустой `ScenarioSuite`, привязанный к указанному процессу.
