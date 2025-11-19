---
id: ARCH-core-services
title: "Архитектура ключевых сервисов"
type: component
layer: application
owner: '@architect'
version: v2
status: planned
created: 2024-07-29
updated: 2024-07-30
tags: [backend, frontend, infrastructure, scheduler, worker]
depends_on: []
referenced_by: []
---
## Контекст
Этот документ описывает высокоуровневую архитектуру контейнеров (C2) платформы Orchestra, показывая ее внутренние сервисы, их взаимодействие друг с другом, с инфраструктурой и внешними системами.

```mermaid
graph TD
    actor User
    actor CI_CD["CI/CD Pipeline"]

    subgraph "Платформа Orchestra"
        subgraph "Frontend"
            WebApp["orchestra-web (SPA on Nginx)"]
        end

        subgraph "Backend Services"
            API["orchestra-api (REST, Scheduler)"]
            Executor["orchestra-executor (Stateless Worker)"]
            AIService["ai-service (Planner, Assistant)"]
        end

        subgraph "Infrastructure"
            DB[(PostgreSQL + PGVector)]
            MQ[("RabbitMQ")]
        end
    end
    
    subgraph "Внешние системы"
        SUT["System Under Test (SUT)"]
        IDP["Identity Provider (Keycloak)"]
        LLM["Локальная LLM (Ollama)"]
    end

    User -- HTTPS --> WebApp
    CI_CD -- REST API --> API
    WebApp -- "REST API" --> API
    
    API -- "Планирует и ставит задачи" --> MQ
    Executor -- "Забирает задачи" --> MQ
    
    API -- "Управляет состоянием" --> DB
    Executor -- "Обновляет состояние" --> DB
    
    API -- "Запрашивает DataPlan" --> AIService
    AIService -- "Вызывает инструменты" --> API
    
    Executor -- "Выполняет шаги" --> SUT
    
    API -- "Валидирует токен" --> IDP
    AIService -- "Вызывает модель" --> LLM
```

## Структура
*   **orchestra-web (SPA)**: Пользовательский интерфейс на React/TypeScript, обслуживаемый Nginx.
*   **orchestra-api (REST, Scheduler)**: Основной бэкенд. Предоставляет REST API, управляет доменными сущностями и содержит **`Scheduler`**, который отвечает за планирование и постановку задач в очередь. Также включает компонент **`Data Resolver`**.
*   **orchestra-executor (Stateless Worker)**: Безсостоящий воркер, который забирает задачи из RabbitMQ, выполняет их и персистентно сохраняет состояние в PostgreSQL после каждого шага.
*   **ai-service (Planner, Assistant)**: "AI Core" системы. Инкапсулирует логику взаимодействия с LLM (через Ollama), выступая в роли планировщика данных, аналитика и помощника для UI.
*   **PostgreSQL**: Основное хранилище данных, являющееся **единственным источником правды** о состоянии `TestRun`. Использует расширения **PGVector** (для RAG) и **RLS** (для безопасности).
*   **RabbitMQ**: Брокер сообщений, используемый как **триггер** для `orchestra-executor`.

## Поведение (Обновлено)
1.  Пользователь или CI/CD через API запускает `TestRun` или `SuiteRun`.
2.  **orchestra-api** создает запись в **PostgreSQL** со статусом **`PENDING`**.
3.  **Scheduler** (внутри `orchestra-api`) периодически анализирует `PENDING` задачи, применяет лимиты и приоритеты, переводит готовую задачу в статус `QUEUED` и отправляет ее ID в **RabbitMQ**.
4.  **orchestra-executor** получает ID задачи, "арендует" ее в **PostgreSQL** (меняя статус на `IN_PROGRESS`) и начинает выполнение.
5.  В процессе выполнения `orchestra-executor` взаимодействует с **SUT** и после каждого шага транзакционно обновляет состояние (`ExecutionContext`, `heartbeat`) в **PostgreSQL**.
6.  При необходимости сгенерировать данные, `orchestra-api` обращается к **ai-service** за "планом данных" (`DataPlan`), который затем исполняется `Data Resolver`'ом.
