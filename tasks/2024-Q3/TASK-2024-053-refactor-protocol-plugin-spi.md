---
id: TASK-2024-053
title: "Задача 3.2.1 (Backend): Рефакторинг для создания ProtocolPlugin SPI"
status: backlog
priority: high
type: refactoring
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-023]
dependencies: [TASK-2024-052]
arch_refs: [ARCH-protocol-plugins]
---
## Описание
Провести рефакторинг `TestRunExecutorService` для внедрения `ProtocolPlugin` SPI. Существующая логика выполнения HTTP-шагов должна быть вынесена в `HttpProtocolPlugin`.

## Ключевые шаги
1. Создать интерфейс `ProtocolPlugin` с методами `supports(channelType)` и `execute(step, context)`.
2. Создать `ProtocolRegistry` для хранения и получения плагинов.
3. Создать `HttpProtocolPlugin` и перенести в него всю логику, связанную с `RestTemplate` из `TestRunExecutorService`.
4. Изменить `TestRunExecutorService` так, чтобы он использовал `ProtocolRegistry` для делегирования выполнения шага соответствующему плагину.

## Критерии приемки
- Существующая функциональность выполнения HTTP-тестов продолжает работать, но уже через новую плагинную архитектуру.
