---
id: TASK-2024-018
title: "Задача 2.3 (Backend/Frontend): Версионирование артефактов (TestScenario)"
status: backlog
priority: medium
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-015]
arch_refs: [ARCH-data-model]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Внедрить механизм версионирования для `TestScenario`.

## Критерии приемки
- (Backend) При изменении `TestScenario` создается его новая версия (copy-on-write).
- (Backend) `TestRun` жестко привязывается к конкретной версии сценария.
- (Frontend) UI отображает номер версии сценария и позволяет просматривать историю версий.
