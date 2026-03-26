---
id: TASK-2024-025
title: "Задача 3.4 (Backend/Frontend): Управление окружениями, коннекторами и секретами"
status: backlog
priority: medium
type: feature
estimate: 16h
created: 2024-07-29
updated: 2024-07-29
parents: [TASK-2024-021]
arch_refs: [ARCH-data-management, ARCH-security-multitenancy]
audit_log:
  - {date: 2024-07-29, user: "@AI-DocArchitect", action: "created with status backlog"}
---
## Описание
Реализовать сущности `Environment`, `DbConnectionProfile` и др. для управления подключениями к SUT. Реализовать интерфейс `SecretProvider` и его интеграцию с целевым Secret Manager.

## Критерии приемки
- (Backend/Frontend) Реализован CRUD для `Environment` и профилей подключений.
- (Backend) Учетные данные для SUT безопасно извлекаются из Secret Manager (или переменных окружения для dev).
- (Frontend) При запуске теста можно выбрать `Environment`.
