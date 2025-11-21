---
id: TASK-2024-024
title: "Задача 3.3 (Backend): Реализация ASSERTION-шагов для Kafka"
status: done
priority: medium
type: feature
estimate: 8h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-021]
arch_refs: [ARCH-protocol-plugins]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать `ProtocolPlugin` для Kafka, который позволит выполнять шаги типа `ASSERTION` для проверки наличия и содержимого сообщений в топиках Kafka.

## Критерии приемки
- В конструкторе сценариев можно добавить шаг "Проверка в Kafka".
- Шаг подключается к Kafka, вычитывает сообщения из топика и ищет совпадение по ключу/содержимому.
- Шаг поддерживает polling с таймаутом.
