---
id: TASK-2024-017
title: "Задача 2.2 (Backend): Интеграция с ai-service для генерации данных"
status: superseded
priority: medium
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-30
parents: [TASK-2024-015]
superseded_by: [TASK-2024-033]
arch_refs: [ARCH-core-services, ARCH-data-management]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "status changed to superseded, superseded_by TASK-2024-033"}
---
## Описание
**Эта задача заменена более детальным планом, описанным в `TASK-2024-033`.**

Изначальная задача предполагала простую интеграцию orchestra-api с ai-service. В ходе проработки было принято решение о создании полноценного, независимого ai-service с использованием локальной LLM (через Ollama) и фреймворка Spring AI для обеспечения надежного структурированного вывода.

Вся работа по реализации AI-функциональности будет отслеживаться в рамках эпика TASK-2024-033 и его дочерних задач.
