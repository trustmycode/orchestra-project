---
id: ARCH-core-services
title: "Архитектура ключевых сервисов"
type: component
layer: application
owner: '@architect'
version: v1
status: planned
created: 2024-07-29
updated: 2024-07-29
tags: [backend, frontend, infrastructure]
depends_on: []
referenced_by: []
---
## Контекст
Этот документ описывает высокоуровневую архитектуру контейнеров (C2) платформы Orchestra, показывая ее внутренние сервисы, их взаимодействие друг с другом, с инфраструктурой и внешними системами.

```mermaid
graph LR
    actor User

    subgraph "Платформа Orchestra"
        subgraph "Frontend"
            WebApp["orchestra-web (SPA)"]
        end

        subgraph "Backend Services"
            API["orchestra-api (REST)"]
            Executor["orchestra-executor (Worker)"]
            AIService["ai-service"]
        end

        subgraph "Infrastructure"
            DB[(PostgreSQL)]
            MQ[("RabbitMQ")]
        end
    end

    User -- HTTPS --> WebApp
    WebApp -- "REST API" --> API
    API -- "Создает задачи" --> MQ
    Executor -- "Забирает задачи" --> MQ
    API -- "Сохраняет/Читает данные" --> DB
    Executor -- "Пишет результаты" --> DB
```

## Структура
*   **orchestra-web (SPA)**: Пользовательский интерфейс, разработанный на React/TypeScript. Отвечает за все взаимодействия с пользователем.
*   **orchestra-api (REST)**: Основной бэкенд-сервис. Предоставляет REST API для UI и внешних систем (CI/CD), управляет доменными сущностями, создает задачи на исполнение.
*   **orchestra-executor (Worker)**: Сервис-воркер, который забирает задачи на исполнение тестов из очереди (RabbitMQ) и выполняет их, взаимодействуя с тестируемой системой (SUT).
*   **ai-service**: Вспомогательный сервис, инкапсулирующий логику взаимодействия с внешними AI-провайдерами (LLM) для генерации данных и анализа отчетов.
*   **PostgreSQL**: Основное хранилище данных для всех доменных сущностей.
*   **RabbitMQ**: Брокер сообщений, используемый для организации очереди задач для `orchestra-executor`.

## Поведение
1.  Пользователь через **WebApp** создает или запускает тестовый сценарий.
2.  **WebApp** отправляет запрос в **orchestra-api**.
3.  **orchestra-api** сохраняет состояние в **PostgreSQL** и отправляет задачу на выполнение в очередь **RabbitMQ**.
4.  **orchestra-executor** получает задачу из **RabbitMQ**, выполняет шаги сценария, взаимодействуя с SUT, и записывает результаты обратно в **PostgreSQL**.
5.  В процессе работы **orchestra-api** может обращаться к **ai-service** для обогащения данных или анализа.

## Эволюция
### Планируемые изменения
— На начальном этапе (MVP) `orchestra-executor` может быть реализован в виде упрощенной очереди в БД, с последующим переходом на RabbitMQ для масштабируемости.
