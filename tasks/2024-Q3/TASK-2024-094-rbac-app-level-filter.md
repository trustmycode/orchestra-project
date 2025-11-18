---
id: TASK-2024-094
title: "Задача 4.1.2 (Backend): Фильтрация и установка tenant_id на уровне приложения"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-028]
dependencies: [TASK-2024-093]
arch_refs: [ADR-0012, ARCH-security-multitenancy]
audit_log:
  - {date: 2024-07-30, user: "@AI-Codex", action: "created"}
---
## Описание
Заставить Spring Boot-сервис автоматически применять `tenant_id` для всех запросов в соответствии с `ADR-0012`: извлекать его из JWT, фильтровать ORM-запросы и прокидывать переменную в PostgreSQL для корректной работы RLS.

## Ключевые шаги
1. Добавить `TenantContext` (`ThreadLocal`) и Request Filter, который извлекает `tenant_id` из `Authentication` и сохраняет в контекст.
2. Реализовать Hibernate Interceptor или `TransactionSynchronization`, который выполняет `SET LOCAL app.current_tenant = :tenant` (через `set_config(..., true)`) перед каждой транзакцией.
3. Добавить базовый слой репозиториев или Hibernate Filter, автоматически дополняющий запросы условием `tenant_id = :tenant`.
4. Покрыть механизм интеграционными тестами, проверяющими, что запросы не возвращают чужие данные и что `tenant_id` обязателен.

## Критерии приемки
- Каждый защищенный API-запрос работает с корректно установленной переменной `app.current_tenant` на время транзакции.
- Все JPA/SQL-запросы имеют явное ограничение по `tenant_id` или используют проверенный Hibernate Filter.
- Интеграционные тесты подтверждают блокировку попыток чтения чужих данных.
