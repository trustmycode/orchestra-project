---
id: TASK-2024-033
title: "Эпик: Реализация `ai-service` на базе Spring AI (Java Only)"
status: backlog
priority: medium
type: feature
estimate: 40h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-015]
children: [TASK-2024-034, TASK-2024-035, TASK-2024-036, TASK-2024-037, TASK-2024-038, TASK-2024-097, TASK-2024-116]
arch_refs: [ARCH-core-services, ADR-0016, ADR-0031, ADR-0032]

---

## Описание

Создать и настроить сервис `ai-service` как чистое Java-приложение на базе **Spring AI**. Сервис будет использовать локальную LLM **`gpt-oss:20b`** (через Ollama) для реализации всех интеллектуальных функций. Промпты должны храниться в базе данных для возможности их оперативной настройки.

## Критерии приемки

- `ai-service` написан полностью на Java (Spring Boot 3.x).
- Используется библиотека `spring-ai`.
- Подключена модель `gpt-oss:20b`.
- Промпты загружаются из БД и кэшируются.
- Реализован базовый механизм структурированного вывода (JSON -> Java DTO).
