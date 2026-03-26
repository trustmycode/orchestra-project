---
id: TASK-2024-130
title: "Задача 4.10.7 (Backend): Сбор полного контекста шагов для генерации на уровне сценария"
status: done
priority: high
type: bugfix
estimate: 4h
created: 2025-11-21
parents: [TASK-2024-037]
dependencies: [TASK-2024-124]
arch_refs: [ADR-0019]
---

## Описание

При вызове `POST /api/v1/ai/data/generate` без указания `stepId` (режим генерации для всего сценария), `ai-service` возвращает пустой результат с комментарием "No specific step or action details were provided".

**Причина:** В классе `AiService.java` блок `if (request.getScenarioId() != null)` загружает сценарий, но детали шагов (action, endpointRef) добавляются в `plannerRequest` только внутри вложенного блока `if (request.getStepId() != null)`.

Необходимо изменить логику сбора контекста, чтобы при отсутствии `stepId` в запрос к AI включался список **всех** шагов сценария.

## Техническое решение

1.  **Модификация `AiService.java`:**

    - Внутри блока загрузки сценария:
      ```java
      if (request.getStepId() != null) {
          // Старая логика для одного шага
      } else {
          // НОВАЯ ЛОГИКА
          List<Map<String, Object>> stepsContext = scenario.getSteps().stream()
              .map(step -> Map.of(
                  "stepId", step.getId().toString(),
                  "alias", step.getAlias(),
                  "name", step.getName(),
                  "kind", step.getKind(),
                  "action", step.getAction() != null ? step.getAction() : Map.of(),
                  "endpointRef", step.getEndpointRef() != null ? step.getEndpointRef() : Map.of()
              ))
              .collect(Collectors.toList());

          plannerRequest.put("steps", stepsContext);
      }
      ```

2.  **Ожидаемое поведение:**
    - `ai-service` (DataPlannerAgent) получит массив `steps`.
    - LLM увидит структуру всех шагов и сгенерирует объединенный JSON.

## Критерии приемки

- [ ] Повторный запуск Кейса №4 (из плана тестирования TASK-2024-037) возвращает заполненный JSON с данными для `http_step` и `db_step`.
- [ ] Поле `admin_user` в этом JSON корректно резолвится в UUID (благодаря TASK-2024-129).
