---
id: TASK-2024-046
title: "Задача 4.4.5 (Backend): Сборка и сохранение TestScenario"
status: backlog
priority: medium
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-031]
dependencies: [TASK-2024-045]
arch_refs: []
---
## Описание
Реализовать финальный шаг генератора: преобразование найденных путей и сопоставленных эндпоинтов в доменные объекты `TestScenario` и `ScenarioStep` и их сохранение в БД.

## Ключевые шаги
1. Для каждого пути создать объект `TestScenario`.
2. Для каждой сопоставленной задачи в пути создать объект `ScenarioStep`, заполнив поля `alias`, `name`, `endpointRef`, `expectations` и т.д.
3. Связать все созданные `TestScenario` с родительским `ScenarioSuite`.
4. Сохранить `ScenarioSuite` (вместе со всеми дочерними объектами) в базу данных.

## Критерии приемки
- После выполнения генерации в БД появляются новые записи в таблицах `scenario_suites`, `test_scenarios`, `scenario_steps`.
