---
id: TASK-2024-033
title: "Эпик: Реализация `ai-service` на базе `sgr-agent-core` и Ollama"
status: backlog
priority: medium
type: feature
estimate: 32h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-015]
children: [TASK-2024-034, TASK-2024-035, TASK-2024-036, TASK-2024-037, TASK-2024-038, TASK-2024-097]
arch_refs: [ARCH-core-services, ADR-0016, ADR-0017, ADR-0025]
---
## Описание
Создать и настроить сервис `ai-service`, который будет служить единым интеллектуальным центром системы. Сервис будет использовать локальную LLM (через Ollama) и фреймворк `sgr-agent-core` для реализации всех AI-функций в виде агентов.

## Критерии приемки
- `ai-service` развернут и доступен.
- Интегрирован `sgr-agent-core`.
- Настроен доступ к локальной Ollama.
- Реализован базовый механизм структурированного вывода (JSON).
