# Orchestra – Архитектура (v4, full)

Этот документ описывает целевую архитектуру системы Orchestra с учётом сложных BPMN-сценариев, управления тестовыми данными, асинхронных шагов, безопасности, масштабирования Execution Engine, версионирования и развёртывания через Docker. Документ самодостаточный, но концептуально совместим с версией v3.

---

## 1. Цели и позиционирование

Orchestra — платформа для тестирования многошаговых бизнес-процессов, описанных в BPMN и/или sequence-диаграммах, поверх распределённых микросервисных систем.

Ключевые цели:

1. Тестировать цепочки действий и побочных эффектов, а не отдельные эндпоинты.
2. Поддерживать несколько протоколов и источников побочных эффектов: HTTP/OpenAPI, Kafka, gRPC, базы данных и очереди.
3. Давать команде удобный конструктор сценариев, управляемые наборы тестовых данных, версионирование артефактов и понятные отчёты с рекомендациями от AI.

## 1.1. Технологический стек и ключевые ограничения

Эта архитектура разработана с учетом требований и ограничений, заданных в рамках задачи хакатона, и ориентирована на создание современного и производительного решения.

1.  **Платформа и язык программирования:**

    - **Требование:** Java 17+.
    - **Решение:** Бэкенд-компоненты (`orchestra-api`, `orchestra-executor`, `ai-service`) будут реализованы на **Java 21 (LTS)**. Этот выбор обеспечивает долгосрочную поддержку, доступ к последним возможностям языка (виртуальные потоки Project Loom, улучшенный pattern matching) и высокую производительность, что полностью соответствует и превосходит исходное требование.

2.  **Формат решения:**

    - **Требование:** Веб-приложение.
    - **Решение:** Система представляет собой **полноценное веб-приложение**, состоящее из одностраничного фронтенда (`orchestra-web` на React) и бэкенд-сервисов, предоставляющих REST API. Это полностью соответствует требованию хакатона о формате решения.

3.  **Поддержка протоколов API:**

    - **Требование:** Поддержка RESTful API.
    - **Решение:** Система обеспечивает **полную поддержку RESTful API** через `HttpProtocolPlugin` на базе спецификации OpenAPI 3.0. Это является основной и первоочередной реализацией в рамках плагинной модели протоколов (ADR-0002). Архитектура также позволяет в будущем расширять поддержку на другие протоколы (gRPC, Kafka Actions), превосходя минимальные требования.

4.  **Ограничения на использование ИИ:**
    - **Требование:** Использование только публичных или локальных ИИ-моделей.
    - **Решение:** В соответствии с требованием, система спроектирована для работы с **публичными или локально развернутыми ИИ-моделями**. Архитектурное решение о выделении `ai-service` (ADR-0005) позволяет инкапсулировать логику взаимодействия с любой конкретной LLM. Это дает возможность легко переключаться между моделями (например, GPT для прототипирования и локальная Llama/Mistral для production или выполнения требований хакатона) без изменения основного кода приложения.

---

## 2. Основные возможности (функциональный обзор)

1. **Импорт процессов** – BPMN 2.0.
2. **Импорт спецификаций SUT** – OpenAPI 3.0.
3. **Генерация и управление сценариями** – **(Обновлено)** построение `ScenarioSuite` из процесса с использованием критериев покрытия (Node/Edge/Decision Coverage) и техник оптимизации (Pairwise) для генерации минимально достаточного набора `TestScenario`.
4. **Управление тестовыми данными** – `TestDataSet` с параметризацией, AI-генерация с использованием паттерна "Planner + Resolver" и RAG для контекстной валидности.
5. **Выполнение сценариев** – **(Обновлено)** отказоустойчивый `Execution Engine` на базе "Durable Workflow", поддерживающий параллельное и асинхронное выполнение, `BARRIER`-синхронизацию и продвинутые режимы отладки.
6. **Оркестрация наборов сценариев** – **(Новое)** запуск `ScenarioSuite` как единого целого (`SuiteRun`) с управлением зависимостями по данным и потоку управления между сценариями.
7. **Побочные эффекты и ASSERTION-шаги** – DB/Kafka/Queue ASSERT на polling-модели с таймаутами и изоляцией через `correlationId`.
8. **Отчёты** – структурированные отчёты с метриками "settling time" и рекомендациями от AI.
9. **Безопасность и мультиарендность** – **(Обновлено)** RBAC и строгая изоляция данных через фильтры в приложении и Row-Level Security (RLS) в PostgreSQL.
10. **Версионирование и совместная работа** – версии процессов/спек/сценариев, история изменений.
11. **Развёртывание через Docker** – локальный `docker-compose`, готовность к Kubernetes.

---

## 3. Контекст и контейнеры (C4)

### 3.1. Акторы

- **QA Engineer / Test Designer**
- **Business Analyst**
- **Dev/Team Lead**
- **CI/CD Pipeline** – автоматические прогоны.
- **SUT** – микросервисы, БД, Kafka, очереди.
- **IDP/SSO** – Keycloak или другой OIDC-провайдер.
- **AI-провайдер** – внешняя LLM.

### 3.2. Контейнеры

- **orchestra-web** – SPA (React + TypeScript), статика через nginx.
- **orchestra-api** – REST API, доменная логика и **Scheduler**. Отвечает за управление артефактами, пользователями и планирование запусков.
- **orchestra-executor** – **Stateless Worker**. Выполняет тестовые шаги, которые ему делегирует `Scheduler` через очередь задач.
- **ai-service** – **"AI Core" / LLM Gateway**. Центральный сервис для всех интеллектуальных задач. Выступает в роли **планировщика данных (Planner)**, **аналитика отчетов (Analyst)** и **помощника (Assistant)** для UI. Инкапсулирует всю логику взаимодействия с LLM (промпт-инжиниринг, вызовы к Ollama).
- **Data Resolver** – Логический компонент внутри `orchestra-api`, который исполняет `DataPlan` от `ai-service`, делая запросы к тестовой БД.
- **postgres** – Основная БД Orchestra. Использует расширение **PGVector** для семантического поиска.
- **mq** – Очередь задач (RabbitMQ).
- **keycloak** – SSO.
- **observability stack** – Prometheus, Grafana, Loki, Jaeger.

Контейнеры `orchestra-api`, `orchestra-executor` и `ai-service` горизонтально масштабируются.

---

## 4. Доменная модель

Все сущности мультиарендные (tenantId обязателен).

### 4.1. Тенанты и пользователи

```text
Tenant
- id: UUID
- name: string
- settings: jsonb

User
- id: UUID
- externalId: string
- email: string
- displayName: string

UserTenantRole
- userId: UUID
- tenantId: UUID
- role: enum { ORG_ADMIN, TEST_DESIGNER, TEST_RUNNER, VIEWER }
```

### 4.2. Процессы и версии

```text
Process
- id: UUID
- tenantId: UUID
- key: string
- createdAt, createdBy

ProcessVersion
- id: UUID
- processId: UUID
- version: int
- sourceType: enum { BPMN, SEQUENCE }
- sourceUri: string
- controlFlowGraph: jsonb
- isPublished: bool
- createdAt, createdBy
```

`controlFlowGraph` описывает узлы, gateway-типы, параллельность и циклы (с ограничениями maxLoopIterations).

### 4.3. Спецификации SUT

```text
ProtocolSpec
- id: UUID
- tenantId: UUID
- protocolId: string ("http", "kafka", "grpc", ...)
- serviceName: string
- version: string
- rawSpecUri: string
- parsedSummary: jsonb
- createdAt, createdBy
```

### 4.4. ScenarioSuite и TestScenario

```text
ScenarioSuite
- id: UUID
- tenantId: UUID
- processId: UUID
- processVersion: int
- name: string
- tags: string[]
- createdAt, createdBy
```

```text
TestScenario
- id: UUID
- tenantId: UUID
- suiteId: UUID
- key: string
- name: string
- version: int
- status: enum { DRAFT, PUBLISHED, DEPRECATED }
- tags: string[]
- active: bool
- steps: [ScenarioStep]
- dependsOn: jsonb // описание зависимостей от других сценариев в Suite
- createdAt, createdBy
- updatedAt, updatedBy
```

`ProcessToScenarioGenerator` строит `ScenarioSuite`, применяя критерии покрытия и техники оптимизации:
- **Exclusive/Inclusive Gateway:** Для покрытия всех решений используется Decision Coverage; для `Inclusive` применяется Pairwise-подбор комбинаций, чтобы сократить количество сценариев без потери рисков.
- **Parallel Gateway:** Генерируется базовый сценарий с последовательным выполнением веток и финальным `BARRIER`-шагом, а также дополнительный набор сценариев с перестановкой веток, если они работают с общими ресурсами (поиск race conditions).
- **Циклы:** Используется стратегия «0-1-N» с ограничениями `maxLoopIterations`.

### 4.5. Шаги сценария

```text
enum StepKind { ACTION, ASSERTION, BARRIER } // BARRIER синхронизирует параллельные ветки
enum ChannelType { HTTP_REST, KAFKA, GRPC, DB, QUEUE }
enum ActionMode { SYNC, FIRE_AND_FORGET }
```

```text
EndpointRef
- protocolId: string
- serviceName: string
- endpointName: string
```

```text
ActionDefinition
- inputTemplate: jsonb
- meta: jsonb { ..., exportAs?: jsonb } // exportAs описывает, какие данные шага записать в SuiteRun.context
- mode: ActionMode
```

Примеры meta:

- HTTP: timeout, headers.
- DB: dataSource, sql, timeoutMs, pollIntervalMs.
- Kafka ASSERT: clusterAlias, topic, keyExpression, valueMatcherMode.
- Queue ASSERT: brokerAlias, queueName, correlationIdExpr, timeoutMs.
- Kafka ACTION (post-MVP): clusterAlias, topic, keyExpression, valueTemplate.

```text
StepExpectations
- expectedStatusCode?: int
- businessRules: jsonb
- schemaRefs: jsonb
```

```text
ScenarioStep
- id: UUID
- scenarioId: UUID
- orderIndex: int
- alias: string
- name: string
- kind: StepKind
- channelType: ChannelType
- endpointRef: EndpointRef
- action: ActionDefinition
- expectations: StepExpectations
```

`BARRIER`-шаги не выполняют действий в SUT. В `ScenarioStep.action.meta` задаётся список `trackedSteps`, который должен завершиться перед продолжением сценария, и стратегия реакции, если один из шагов завершился с `FAILED`.

### 4.6. TestDataSet и параметризация

```text
TestDataSet
- id: UUID
- tenantId: UUID
- scope: enum { GLOBAL, SUITE, SCENARIO }
- suiteId?: UUID
- scenarioId?: UUID
- name: string
- tags: string[]
- origin: enum { MANUAL, AI_GENERATED, IMPORTED }
- data: jsonb
- createdAt, createdBy
```

`ScenarioStep.action.inputTemplate` использует выражения `{{data.*}}` и `{{step.alias.response.*}}`. При запуске ExecutionContext.variables["data"] = TestDataSet.data.

### 4.7. Окружения и коннекторы

```text
DbConnectionProfile
- id, tenantId, alias, jdbcUrl, usernameRef, passwordRef, defaultSchema

KafkaClusterProfile
- id, tenantId, alias, bootstrapServers, securityProfileRef

QueueBrokerProfile
- id, tenantId, alias, kind, endpoint, credentialsRef

Environment
- id, tenantId, name
- dbProfiles: alias -> DbConnectionProfile.id
- kafkaProfiles: alias -> KafkaClusterProfile.id
- queueProfiles: alias -> QueueBrokerProfile.id
```

Шаги хранят alias, а реальные подключения резолвятся через выбранное Environment.

### 4.8. Выполнение и результаты

```text
enum StepStatus { PENDING, RUNNING, PASSED, FAILED, SKIPPED, FLAKY }

enum ViolationType {
  HTTP_ERROR, CONTRACT_VIOLATION, BUSINESS_RULE_VIOLATION,
  TIMEOUT, VALIDATION_ERROR,
  DB_ASSERTION_FAILED, KAFKA_MESSAGE_NOT_FOUND, QUEUE_MESSAGE_NOT_FOUND
}
```

```text
Violation
- type: ViolationType
- code: string
- message: string
- details: jsonb
- settlingTimeMs?: long // время, потребовавшееся ASSERT-шага для выполнения условия
```

```text
StepResult
- stepId: UUID
- stepAlias: string
- status: StepStatus
- durationMs: long
- actualStatusCode?: int
- responseBody?: jsonb
- responseHeaders?: jsonb
- dbResult?: jsonb
- kafkaMessage?: jsonb
- queueMessage?: jsonb
- violations: [Violation]
```

```text
TestRun
- id: UUID
- tenantId: UUID
- scenarioId: UUID
- scenarioVersion: int
- suiteRunId?: UUID // ссылка на родительский SuiteRun
- dataSetId?: UUID
- environmentId?: UUID
- mode: enum { RUN_ALL_STEPS, STOP_ON_FIRST_FAILURE, SINGLE_STEP, FROM_STEP }
- startStepIndex?: int
- status: enum { PENDING, QUEUED, IN_PROGRESS, PASSED, FAILED, FAILED_STUCK, CANCELLED }
- startedAt, finishedAt
- correlationId: string
- lockedBy?: string
- lockUntil?: timestamp
- heartbeatAt?: timestamp
- executionContext: jsonb // Durable контекст
- createdBy, triggeredBy
```

```text
SuiteRun
- id: UUID
- tenantId: UUID
- suiteId: UUID
- status: enum { PENDING, IN_PROGRESS, PASSED, FAILED }
- context: jsonb // общий контекст для сценариев
- startedAt, finishedAt
```

```text
ExecutionContext
- runId: UUID
- stepsByAlias: map<string, StepResult>
- variables: jsonb // включает data, step responses и ссылки на suite.context
```

`SuiteRun.context` накапливает экспортированные данные (`ScenarioStep.action.meta.exportAs`). Шаги последующих сценариев могут ссылаться на них через `{{suite.*}}`, а зависимое выполнение определяется по `TestScenario.dependsOn`.

---

## 5. Execution Engine и масштабирование

### 5.1. Архитектура "Durable Workflow"

Состояние выполнения хранится в PostgreSQL, а воркеры `orchestra-executor` работают как stateless-исполнители:

- **Durable ExecutionContext.** После каждого шага в транзакции обновляются `test_step_results`, `test_runs.status`, `execution_context` и `heartbeat_at`.
- **Lease-паттерн.** Захват задачи происходит через `UPDATE test_runs SET ... WHERE status='QUEUED' LIMIT 1 RETURNING *`, что исключает жёсткие блокировки.
- **Scheduler как центральная точка принятия решений.** Очередь (`mq`) используется как триггер для воркеров, а не как источник правды.

### 5.2. Жизненный цикл прогона (`TestRun`)

1. **Планирование (`Scheduler`).**
   - `POST /testruns` создаёт запись со статусом `PENDING`.
   - Периодический `Scheduler` в `orchestra-api` анализирует очередь `PENDING`, учитывает лимиты на тенанта/приоритеты и переводит выбранные прогоны в `QUEUED`, публикуя ID в RabbitMQ.
2. **Выполнение (`Executor`).**
   - Воркер читает сообщение, берёт "аренду" на TestRun (обновляя `lockedBy`, `lockUntil`).
   - Загружает `ExecutionContext`, выполняет шаги, после каждого шага обновляет состояние и `heartbeat_at`.
   - При завершении выставляет финальный статус (PASSED/FAILED/CANCELLED).
3. **Отказоустойчивость и контроль зависаний.**
   - При падении воркера сообщение возвращается в очередь; новый воркер продолжит с последнего успешного шага.
   - `Reaper` отслеживает истечение аренды или устаревший `heartbeat_at` и переводит прогон в `FAILED_STUCK`.

### 5.3. Обработка параллельных и асинхронных операций

- **`FIRE_AND_FORGET` + `ASSERT` с polling'ом:** Базовый паттерн для тестирования eventual consistency. ACTION инициирует асинхронный процесс, а последующие ASSERTION-шаги (DB/Kafka/Queue) с polling-моделью подтверждают побочные эффекты в пределах `timeoutMs`.
- **`BARRIER` Step:** Синхронизирует параллельные ветки, запущенные в `FIRE_AND_FORGET`. `BARRIER` блокирует сценарий, пока все указанные `trackedSteps` не завершатся (PASSED/FAILED) и позволяет задать реакцию на ошибки (прервать SuiteRun или продолжить).
- **Изоляция:** Каждый `TestRun` получает уникальный `correlationId`, автоматически добавляемый во все исходящие вызовы. ASSERTION фильтруют события по этому идентификатору, исключая "шумного соседа".

### 5.4. Режимы исполнения и отладка

- `RUN_ALL_STEPS` – выполняет все шаги, фиксируя все нарушения.
- `STOP_ON_FIRST_FAILURE` – при первом `FAILED` дальнейшие шаги помечаются `SKIPPED`.
- `SINGLE_STEP` – выполнение одного шага с указанным ExecutionContext (для UI-отладки).
- `FROM_STEP` – переиспользует результаты предыдущего прогона до `startStepIndex` и продолжает сценарий.

### 5.5. ProtocolPlugin SPI

Цель – изолировать работу с конкретными протоколами.

```text
ProtocolPlugin
- id: string
- specParser: ProtocolSpecParser
- endpointMatcher: EndpointMatcher
- stepExecutor: StepExecutor
- contractValidator: ContractValidator
```

- http-plugin – полноценный (OpenAPI, JSON Schema).
- kafka-plugin – ASSERTION (consume/poll), ACTION publish post-MVP.
- db-plugin – ASSERTION (SELECT + проверка правил).
- queue-plugin – ASSERTION для очередей.
- grpc-plugin – добавляется по необходимости.

`orchestra-executor` выбирает плагин по `ScenarioStep.channelType` и делегирует выполнение.

---

## 6. Оркестрация `SuiteRun` и зависимости

### 6.1. `SuiteRun` Orchestrator

`SuiteRun Orchestrator` (часть `Scheduler`'а) управляет графом зависимостей в рамках одного запуска.

1. **Инициация:** При запуске `SuiteRun` создаются все дочерние `TestRun` со статусом `PENDING`.
2. **Анализ графа:** Оркестратор строит граф зависимостей на основе поля `TestScenario.dependsOn`.
3. **Пошаговый запуск:** В очередь на выполнение отправляются только те `TestRun`, чьи зависимости удовлетворены (родительские сценарии завершились нужным статусом).
4. **Цикл:** Процесс повторяется по мере завершения `TestRun` до полного выполнения графа.

### 6.2. Передача данных между сценариями

- **`SuiteRun.context`:** Общее JSONB-хранилище для сценариев одного запуска.
- **Экспорт (`exportAs`):** Шаг может экспортировать часть своего результата в `SuiteRun.context`.
- **Импорт (`{{suite.*}}`):** Последующие сценарии используют плейсхолдеры для чтения данных, предоставляя data dependencies через Orchestrator.

---

## 7. Управление тестовыми данными и AI-возможности

### 7.1. Генерация данных: Паттерн "Planner + Resolver"

- **`ai-service` (Planner):** По запросу пользователя `ai-service` выступает в роли планировщика. Он анализирует контекст (схему API, описание сценария) и формирует `DataPlan` — структурированные требования к данным (например, "нужен активный клиент из сегмента 'retail'").
- **`Data Resolver` (Tool в `orchestra-api`):** Этот компонент принимает `DataPlan`, анализирует кастомные расширения OpenAPI (`x-orchestra-entity`, `x-orchestra-resolver`), строит детерминированные SQL/DSL-запросы и возвращает реальные данные из тестовой БД.
- **RAG на PGVector:** Для семантических критериев (`"клиент, похожий на..."`) `Data Resolver` использует pgvector для выполнения гибридных запросов (`embedding <-> :query`), обогащая SQL-фильтрацию.
- **Гарантия валидности:** Результат собирается из реальных записей тестовой БД и сохраняется в `TestDataSet`.

### 7.2. Другие роли `ai-service`

Помимо планирования данных, `ai-service` выполняет и другие ключевые интеллектуальные функции, выступая в роли центрального "AI Core":

- **Аналитик отчетов (`Analyst`):** После завершения `TestRun` `orchestra-api` может отправить результаты в `ai-service` для анализа. Сервис генерирует рекомендации на естественном языке по улучшению тестового покрытия, исправлению ошибок или доработке спецификаций.
- **Помощник (`Assistant`):** `ai-service` предоставляет "умные" подсказки для UI в реальном времени:
  - **Сопоставление (Mapping):** Предлагает наиболее вероятное сопоставление между BPMN-задачей и API-эндпоинтом во время генерации сценария.
  - **Передача данных (Data Transfer):** Предлагает готовые плейсхолдеры для связи выхода одного шага со входом другого (`{{steps.alias.response.body.id}}`).
  - **Предложение проверок (Assertion Suggestion):** Анализирует `ACTION`-шаг и предлагает релевантные `ASSERTION`-шаги для проверки побочных эффектов в БД или Kafka.

### 7.3. Связка с сценариями и прогонами

- ScenarioSuite/TestScenario могут иметь рекомендованные наборы данных.
- `TestRunCreateRequest` принимает `dataSetId`, `dataSetTag` или `dataPlan`. В последнем случае orchestrator вызывает Planner/Resolver на лету.
- Экспорт переменных через `SuiteRun.context` позволяет комбинировать данные между сценариями (`{{suite.*}}`).

### 7.4. Отчёты по данным

Отчёт `TestRun` содержит ссылку на использованный TestDataSet, детали `DataPlan` и рекомендации AI по улучшению покрытия (например, какие критерии не нашли данных).

---

## 8. Безопасность и мультиарендность

### 8.1. Аутентификация

- OIDC-провайдер (например, Keycloak).
- `orchestra-web` получает access/refresh token.
- `orchestra-api` валидирует JWT, извлекает `sub`, `tenantId`, `roles`.

### 8.2. Авторизация и изоляция данных

- **RBAC:** Доступ к функциям ограничен ролями (`ORG_ADMIN`, `TEST_DESIGNER`, `TEST_RUNNER`, `VIEWER`). Роли определяют разрешения на импорт артефактов, запуск сценариев, управление пользователями и окружениями.
- **Двухуровневая изоляция данных:**
  1. **Уровень приложения.** Все запросы автоматически фильтруются по `tenantId` (Hibernate Filters/Specifications), что исключает смешение данных при корректном коде.
  2. **Уровень БД (RLS).** В PostgreSQL включается Row-Level Security с отдельными политиками для каждой таблицы. Даже при ошибке в приложении БД не выдаст строки чужого тенанта.
- **Защита от "шумного соседа":**
  - **Scheduler:** Применяет квоты `maxParallelRuns` и приоритеты на уровне тенанта, чтобы один клиент не выжигал все воркеры.
  - **RabbitMQ:** При необходимости создаются отдельные очереди/virtual host на класс тенанта (SLA), что позволяет ограничивать влияние bursts трафика.

---

## 9. Хранилище и ретеншн

### 9.1. Основные таблицы

`tenant`, `user`, `user_tenant_role`, `process`, `process_version`, `protocol_spec`, `scenario_suite`, `test_scenario`, `scenario_step`, `test_data_set`, `environment`, `db_connection_profile`, `kafka_cluster_profile`, `queue_broker_profile`, `test_run`, `test_step_result`, `test_run_report`, `audit_log`.

Гибкие структуры (controlFlowGraph, action, expectations, data, results) хранятся в jsonb. Индексы: b-tree по `(tenantId, scenarioId, startedAt)`, GIN по `test_step_result.payload` для аналитики.

### 9.2. Политики хранения

```text
TenantSettings
- defaultRunRetentionDays: int (по умолчанию 90)
- maxParallelRuns: int
- maxScenariosPerTenant: int
```

Фоновые задачи `cleanup_old_runs` удаляют/архивируют `test_run` и связанные `test_step_result` старше N дней, опционально архивируя в S3/Minio. Возможна индексация агрегатов и архивирование отчётов.

---

## 10. Версионирование и совместная работа

### 10.1. Версионирование

- ProcessVersion, ProtocolSpec.version, TestScenario.version.
- Изменение сценария создаёт новую версию (copy-on-write); старая помечается DEPRECATED или остаётся PUBLISHED.
- CI/прогоны ссылаются на `(scenarioId, version)` или стабильные теги.

### 10.2. Совместная работа и блокировки

- Оптимистичные блокировки по `updatedAt/version`, UI сообщает о конфликтах.
- Post-MVP: `lock for edit` на уровне TestScenario (чтобы один пользователь редактировал, остальные читали).

---

## 11. Наблюдаемость и эксплуатация

### 11.1. Метрики

Примеры:

- `orchestra_testrun_total{tenant,status}`
- `orchestra_step_duration_ms{tenant,channelType,kind,status}`
- `orchestra_assertion_timeouts_total{tenant,channelType}`
- `orchestra_flaky_steps_total{tenant}`

### 11.2. Логирование

Структурированные JSON-логи с полями `tenantId`, `runId`, `scenarioId`, `stepAlias`, `channelType`, `kind`, `status`. Для ASSERTION логируются SQL/ключи/корреляции без чувствительных данных.

### 11.3. Трейсинг

OpenTelemetry spans на TestRun и на каждый ScenarioStep.

---

## 12. Развёртывание через Docker

### 12.1. Контейнеры

Базовый `docker-compose` включает: postgres, mq, orchestra-api, orchestra-executor, orchestra-web, ai-service, keycloak (dev), prometheus/grafana (опционально).

### 12.2. Пример `docker-compose.yml`

`ai-service` разворачивается рядом с остальными бэкендами, но при этом должен иметь сетевой доступ к Ollama, запущенному на машине разработчика. Для этого контейнеру пробрасывается `host.docker.internal` и переменная `OLLAMA_BASE_URL`.

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: orchestra
      POSTGRES_USER: orchestra
      POSTGRES_PASSWORD: orchestra
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  mq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  orchestra-api:
    image: orchestra/api:latest
    depends_on:
      - postgres
      - mq
    environment:
      DB_URL: postgres://orchestra:orchestra@postgres:5432/orchestra?sslmode=disable
      MQ_URL: amqp://guest:guest@mq:5672/
      OIDC_ISSUER: http://keycloak:8080/realms/orchestra
      AI_SERVICE_URL: http://ai-service:8080
    ports:
      - "8080:8080"

  orchestra-executor:
    image: orchestra/api:latest
    command: ["executor"]
    depends_on:
      - postgres
      - mq
    environment:
      DB_URL: postgres://orchestra:orchestra@postgres:5432/orchestra?sslmode=disable
      MQ_URL: amqp://guest:guest@mq:5672/
      OIDC_ISSUER: http://keycloak:8080/realms/orchestra
      AI_SERVICE_URL: http://ai-service:8080

  orchestra-web:
    image: orchestra/web:latest
    depends_on:
      - orchestra-api
    ports:
      - "3000:80"
    environment:
      API_BASE_URL: http://orchestra-api:8080/api/v1

  ai-service:
    image: orchestra/ai-service:latest
    ports:
      - "8090:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"
    environment:
      OLLAMA_BASE_URL: http://host.docker.internal:11434

volumes:
  pgdata: {}
```

### 12.3. Поток развёртывания (dev/hackathon)

1. Сборка образов `orchestra/api`, `orchestra/web`, `orchestra/ai-service`.
2. `docker compose up -d`.
3. Миграции БД при старте `orchestra-api` или отдельной командой `docker compose run --rm orchestra-api migrate`.

---

## 13. MVP vs Full Scope (карта развития)

| Область                | MVP (hackathon)                           | Full (v4)                                               |
| ---------------------- | ----------------------------------------- | ------------------------------------------------------- |
| BPMN/Sequence          | Импорт, линейный путь                     | ScenarioSuite с ветвлениями и циклами                   |
| Протоколы              | HTTP/OpenAPI полноценно, Kafka/DB скелеты | Kafka/Queue ASSERT, Kafka ACTION, gRPC                  |
| TestDataSet            | Базовый AI для HTTP                       | Полные наборы, теги, параметризация всех шагов          |
| Execution Engine       | In-process, sync вызов                    | Очередь заданий + воркеры, режимы FROM_STEP/SINGLE_STEP |
| ASSERTION              | HTTP, базовая DB                          | DB/Kafka/Queue polling, flakiness, метрики              |
| Безопасность           | Single-tenant или простой tenantId        | Полный RBAC, SSO, row-level isolation                   |
| Версионирование        | Простая версия сценария                   | Версии процессов/спек/сценариев, статусы, история       |
| Ретеншн и эксплуатация | Без политик                               | Per-tenant ретеншн, cleanup, observability stack        |
| Docker                 | Минимальный compose                       | Полный compose + готовность к Kubernetes                |

---

Этот документ задаёт целевую архитектуру. При реализации в рамках хакатона допускается подмножество функциональности, но интерфейсы и модели должны соответствовать описанию, чтобы избежать технического долга.
