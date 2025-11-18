---
id: TASK-2024-009
title: "Задача 1.2 (Backend): Реализация импорта артефактов (BPMN, OpenAPI)"
status: backlog
priority: high
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-007]
arch_refs: [ARCH-protocol-plugins]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать на бэкенде API-эндпоинты и логику для импорта файлов BPMN-процессов и спецификаций OpenAPI.

## Критерии приемки
- Создан API-эндпоинт для загрузки BPMN-файла, который создает сущность `Process` в БД.
- Создан API-эндпоинт для загрузки OpenAPI-файла, который создает сущность `ProtocolSpec` в БД.
- Реализованы базовые парсеры для извлечения необходимой информации из файлов.
