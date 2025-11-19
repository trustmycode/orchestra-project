---
id: ARCH-protocol-plugins
title: "Плагинная модель поддержки протоколов"
type: feature
layer: application
owner: '@architect'
version: v2
status: planned
created: 2024-07-29
updated: 2024-07-30
tags: [plugin, spi, extensibility]
depends_on: [ARCH-execution-engine]
referenced_by: []
---
## Контекст
Для поддержки различных протоколов взаимодействия (HTTP, DB, Kafka и др.) без изменения ядра `Execution Engine` используется плагинная архитектура.

## Структура
Вводится сервисный интерфейс `ProtocolPlugin`, который должен быть реализован для каждого поддерживаемого `channelType`.
*   **`supports(String channelType)`**: Метод для определения, может ли плагин обработать данный тип шага.
*   **`execute(ScenarioStep step, ExecutionContext context)`**: Основной метод, выполняющий логику шага.
*   **`ProtocolRegistry`**: Центральный компонент, который хранит и предоставляет экземпляры плагинов.

## Поведение
Когда `orchestra-executor` выполняет шаг, он определяет `channelType` (например, `HTTP_REST`, `DB`), запрашивает у `ProtocolRegistry` соответствующий плагин и делегирует ему выполнение.

### Ключевые аспекты реализации плагинов
*   **Идемпотентность:** Плагины должны быть спроектированы с учетом возможного повторного выполнения шага.
*   **Передача `correlationId`:** Все `ACTION`-плагины, инициирующие внешние вызовы, обязаны добавлять `correlationId` из `ExecutionContext` в запрос (например, в HTTP-заголовки).
*   **Фильтрация по `correlationId`:** Все `ASSERTION`-плагины обязаны использовать `correlationId` для фильтрации побочных эффектов, чтобы обеспечить изоляцию тестов.
*   **Polling:** `ASSERTION`-плагины для асинхронных систем (DB, Kafka) должны реализовывать логику polling'а с `timeoutMs` и `pollIntervalMs`.

## Эволюция
-   **Текущий скоуп:** Реализация `HttpProtocolPlugin`, `DbProtocolPlugin`, `KafkaProtocolPlugin`.
-   **Будущее развитие:** Добавление плагинов для gRPC, очередей (RabbitMQ, SQS) и других протоколов.
