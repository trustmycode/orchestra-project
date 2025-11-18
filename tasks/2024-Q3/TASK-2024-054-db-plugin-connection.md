---
id: TASK-2024-054
title: "Задача 3.2.2 (Backend): Реализация `DbProtocolPlugin` и управления подключениями"
status: backlog
priority: medium
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-023]
dependencies: [TASK-2024-025, TASK-2024-053]
arch_refs: [ARCH-protocol-plugins]
---
## Описание
Создать скелет `DbProtocolPlugin` и реализовать логику получения параметров подключения к БД на основе `Environment`, переданного в `TestRun`.

## Ключевые шаги
1. Создать класс `DbProtocolPlugin`, который регистрируется для `channelType = 'DB'`.
2. Реализовать `ConnectionManager`, который будет кэшировать `DataSource` для разных профилей подключения.
3. В методе `execute` плагина получать `dataSource` из `meta` шага, находить соответствующий `DbConnectionProfile` в `Environment` и получать `DataSource` от `ConnectionManager`.

## Критерии приемки
- `DbProtocolPlugin` может успешно установить соединение с целевой БД, используя конфигурацию из `Environment`.
