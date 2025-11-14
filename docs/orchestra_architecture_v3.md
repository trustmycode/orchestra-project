# Orchestra – Архитектура (v4, full)

Этот документ описывает целевую архитектуру системы Orchestra с учётом сложных BPMN-сценариев, управления тестовыми данными, асинхронных шагов, безопасности, масштабирования Execution Engine, версионирования и развёртывания через Docker. Документ самодостаточный, но концептуально совместим с версией v3.

---

## 1. Цели и позиционирование

Orchestra — платформа для тестирования многошаговых бизнес-процессов, описанных в BPMN и/или sequence-диаграммах, поверх распределённых микросервисных систем.

Ключевые цели:

1. Тестировать цепочки действий и побочных эффектов, а не отдельные эндпоинты.
2. Поддерживать несколько протоколов и источников побочных эффектов: HTTP/OpenAPI, Kafka, gRPC, базы данных и очереди.
3. Давать команде удобный конструктор сценариев, управляемые наборы тестовых данных, версионирование артефактов и понятные отчёты с рекомендациями от AI.

---

## 2. Основные возможности (функциональный обзор)

1. **Импорт процессов** – BPMN 2.0 (XML) и sequence-диаграммы (JSON, совместимый с UML/Swimlane/mermaid) с построением внутренней модели процесса, gateway-ов, параллельных веток и циклов.
2. **Импорт спецификаций SUT** – OpenAPI 3.0 для HTTP/gRPC; расширения под Avro/Schema Registry, protobuf, AsyncAPI.
3. **Генерация и управление сценариями** – построение ScenarioSuite из процесса, где каждая значимая ветка → отдельный `TestScenario`, поддержка параллельных веток и комбинаций.
4. **Управление тестовыми данными** – `TestDataSet` с параметризацией `{{data.*}}`, AI-генерация наборов (HAPPY_PATH/NEGATIVE/BOUNDARY).
5. **Выполнение сценариев** – Execution Engine на очереди заданий с воркерами, режимы `RUN_ALL_STEPS`, `STOP_ON_FIRST_FAILURE`, `SINGLE_STEP`, `FROM_STEP`, поддержка fire-and-forget ACTION.
6. **Побочные эффекты и ASSERTION-шаги** – DB/Kafka/Queue ASSERT на polling-модели с таймаутами и специфичными нарушениями.
7. **Отчёты** – структурированные `TestRun` отчёты, агрегированные ошибки, рекомендации по сценариям, данным и спецификациям с помощью AI.
8. **Безопасность и мультиарендность** – OIDC SSO, RBAC (ORG_ADMIN/TEST_DESIGNER/TEST_RUNNER/VIEWER), жёсткое разделение данных по `tenantId`.
9. **Версионирование и совместная работа** – версии процессов/спек/сценариев со статусами DRAFT/PUBLISHED/DEPRECATED, история изменений и блокировки.
10. **Развёртывание через Docker** – локальный `docker-compose`, готовность к переносу в Kubernetes.

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
- **orchestra-api** – REST API и доменная логика.
- **orchestra-executor** – воркеры, выполняющие сценарии из очереди заданий.
- **ai-service** – адаптер к LLM.
- **postgres** – БД Orchestra.
- **mq** – очередь задач (RabbitMQ / Redis Streams / Kafka topic).
- **keycloak** – SSO (опционально в dev).
- **observability stack** – Prometheus, Grafana, Loki/ELK, Jaeger.

Контейнеры `orchestra-api` и `orchestra-executor` горизонтально масштабируются.

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
- createdAt, createdBy
- updatedAt, updatedBy
```

`ProcessToScenarioGenerator` строит ScenarioSuite: exclusive/inclusive gateway → разветвление сценариев; parallel gateway → комбинации сценариев; циклы → сценарии «0/1/N раз» (ограничено maxLoopIterations).

### 4.5. Шаги сценария

```text
enum StepKind { ACTION, ASSERTION }
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
- meta: jsonb
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
- dataSetId?: UUID
- environmentId?: UUID
- mode: enum { RUN_ALL_STEPS, STOP_ON_FIRST_FAILURE, SINGLE_STEP, FROM_STEP }
- startStepIndex?: int
- status: enum { PENDING, QUEUED, IN_PROGRESS, PASSED, FAILED, CANCELLED }
- startedAt, finishedAt
- createdBy, triggeredBy
```

```text
ExecutionContext
- runId: UUID
- stepsByAlias: map<string, StepResult>
- variables: jsonb
```

---

## 5. Execution Engine и масштабирование

### 5.1. Жизненный цикл прогона

1. `POST /testruns` валидирует права, создаёт TestRun со статусом `QUEUED`, пишет запись в `run_jobs` (БД или MQ) и возвращает TestRunSummary.
2. `orchestra-executor` подписан на очередь `run_jobs`, выбирает `runId`, помечает TestRun как `IN_PROGRESS` (row-lock) и загружает TestScenario, TestDataSet, Environment.
3. Создаётся ExecutionContext, шаги выполняются последовательно, результаты фиксируются в БД.
4. После завершения – запись всех StepResult, обновление статуса TestRun (PASSED/FAILED/CANCELLED) и `finishedAt`.

Воркеры масштабируются горизонтально, очереди гарантируют доставку задач и восстановление после падения.

### 5.2. Поведение ACTION и ASSERTION

- **ACTION/SYNC** – request-response (HTTP/gRPC) с проверкой статуса, схемы, бизнес-правил.
- **ACTION/FIRE_AND_FORGET** – инициирует действие (HTTP, publish в Kafka), успех шага = успешный вызов, побочные эффекты проверяются последующими ASSERTION.
- **ASSERTION (DB/Kafka/Queue)** – polling до `timeoutMs`:
  - DB ASSERT – выполняет SELECT, проверяет businessRules, violations → `DB_ASSERTION_FAILED`.
  - Kafka ASSERT – ищет сообщение по `key/correlationId`, режимы `EXISTS_ONLY`/`PARTIAL_MATCH`, violations → `KAFKA_MESSAGE_NOT_FOUND`.
  - Queue ASSERT – ищет сообщение по `correlationIdExpr`, violations → `QUEUE_MESSAGE_NOT_FOUND`.
  - При превышении таймаута → `TIMEOUT` + специфичный violation.

### 5.3. Режимы исполнения и отладка

- `RUN_ALL_STEPS` – выполняет все шаги, фиксируя все нарушения.
- `STOP_ON_FIRST_FAILURE` – при первом `FAILED` дальнейшие шаги помечаются `SKIPPED`.
- `SINGLE_STEP` – выполнение одного шага с указанным ExecutionContext (для UI-отладки).
- `FROM_STEP` – переиспользует результаты предыдущего прогона до `startStepIndex` и продолжает сценарий.

---

## 6. ProtocolPlugin SPI

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

## 7. Управление тестовыми данными

### 7.1. Создание и хранение

- Ручное создание и клонирование TestDataSet в UI.
- Генерация через AI: вызов `ai-service` с контекстом шага/сценария и режимом (HAPPY_PATH/NEGATIVE/BOUNDARY) → сохранение с `origin = AI_GENERATED`.

### 7.2. Связка с сценариями и прогонами

- ScenarioSuite/TestScenario могут иметь рекомендованные наборы данных.
- `TestRunCreateRequest` принимает `dataSetId` или `dataSetTag` для автоматического подбора набора.

### 7.3. Отчёты по данным

Отчёт `TestRun` содержит ссылку на использованный TestDataSet и рекомендации AI по улучшению покрытия.

---

## 8. Безопасность и мультиарендность

### 8.1. Аутентификация

- OIDC-провайдер (например, Keycloak).
- `orchestra-web` получает access/refresh token.
- `orchestra-api` валидирует JWT, извлекает `sub`, `tenantId`, `roles`.

### 8.2. Авторизация (RBAC)

Роли:

- `ORG_ADMIN` – управление пользователями, окружениями, ретеншн-политиками.
- `TEST_DESIGNER` – импорт процессов/спек, создание сценариев и наборов данных.
- `TEST_RUNNER` – запуск сценариев и просмотр результатов.
- `VIEWER` – только просмотр.

Все запросы фильтруются по `tenantId`, доступ к чужим данным невозможен (проверки в приложении и, при необходимости, в БД через RLS/schema-per-tenant).

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
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY:-dummy}
    ports:
      - "8090:8080"

volumes:
  pgdata: {}
```

### 12.3. Поток развёртывания (dev/hackathon)

1. Сборка образов `orchestra/api`, `orchestra/web`, `orchestra/ai-service`.
2. `docker compose up -d`.
3. Миграции БД при старте `orchestra-api` или отдельной командой `docker compose run --rm orchestra-api migrate`.

---

## 13. MVP vs Full Scope (карта развития)

| Область | MVP (hackathon) | Full (v4) |
| --- | --- | --- |
| BPMN/Sequence | Импорт, линейный путь | ScenarioSuite с ветвлениями и циклами |
| Протоколы | HTTP/OpenAPI полноценно, Kafka/DB скелеты | Kafka/Queue ASSERT, Kafka ACTION, gRPC |
| TestDataSet | Базовый AI для HTTP | Полные наборы, теги, параметризация всех шагов |
| Execution Engine | In-process, sync вызов | Очередь заданий + воркеры, режимы FROM_STEP/SINGLE_STEP |
| ASSERTION | HTTP, базовая DB | DB/Kafka/Queue polling, flakiness, метрики |
| Безопасность | Single-tenant или простой tenantId | Полный RBAC, SSO, row-level isolation |
| Версионирование | Простая версия сценария | Версии процессов/спек/сценариев, статусы, история |
| Ретеншн и эксплуатация | Без политик | Per-tenant ретеншн, cleanup, observability stack |
| Docker | Минимальный compose | Полный compose + готовность к Kubernetes |

---

Этот документ задаёт целевую архитектуру. При реализации в рамках хакатона допускается подмножество функциональности, но интерфейсы и модели должны соответствовать описанию, чтобы избежать технического долга.
