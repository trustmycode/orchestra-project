```markdown

---

id: TASK-2024-126

title: "Задача: Реализация API клиента для AI Planner на фронтенде"

status: done

priority: high

type: feature

estimate: 2h

created: 2024-07-31

updated: 2024-07-31

parents: [TASK-2024-037]

dependencies: []

arch_refs: [ADR-0019, ADR-0016]

audit_log:

  - {date: 2024-07-31, user: "@RoboticArchitect", action: "created"}
  - {date: 2024-07-31, user: "@RoboticAI", action: "implemented"}

---

## Описание

Необходимо обновить слой API на фронтенде (`orchestra-web`), чтобы поддержать взаимодействие с новым эндпоинтом бэкенда `/api/v1/ai/data/generate`. Текущая реализация `generateAiDataSimple` является заглушкой и должна быть заменена или дополнена полноценным типизированным методом.

## Контекст

Бэкенд теперь поддерживает паттерн "Planner + Resolver". Для корректной работы генерации данных необходимо передавать контекст: ID сценария, ID шага и ID окружения (для резолвинга подключений к БД).

## Ключевые шаги

1.  **Обновить `apps/orchestra-web/src/types.ts`:**

    *   Добавить интерфейс `AiGenerateDataRequest`:

        ```typescript

        export interface AiGenerateDataRequest {

          scenarioId?: string;

          stepId?: string;

          mode?: 'HAPPY_PATH' | 'NEGATIVE' | 'BOUNDARY';

          environmentId?: string;

        }

        ```

    *   Добавить интерфейс `AiGenerateDataResponse`:

        ```typescript

        export interface AiGenerateDataResponse {

          data: JsonRecord;

          notes: string;

        }

        ```

2.  **Обновить `apps/orchestra-web/src/api.ts`:**

    *   Реализовать функцию `generateAiData(request: AiGenerateDataRequest): Promise<AiGenerateDataResponse>`.

    *   Функция должна выполнять POST запрос на `/api/v1/ai/data/generate`.

## Критерии приемки

*   В `api.ts` присутствует экспортируемая функция `generateAiData`.

*   Типы запроса и ответа соответствуют контракту OpenAPI (см. `docs/orchestra_openapi_v2.json`).

*   Код компилируется без ошибок TypeScript.

```

