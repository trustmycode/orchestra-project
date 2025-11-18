---
id: TASK-2024-036
title: "Задача 2.2.3: Реализация логики структурированного вывода в `ai-service`"
status: backlog
priority: high
type: task
estimate: 8h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-033]
dependencies: [TASK-2024-035]
arch_refs: [ADR-0016, ADR-0017]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Реализовать основную логику `ai-service` по генерации тестовых данных с использованием Spring AI и `BeanOutputParser` для обеспечения гарантированно валидного JSON-ответа.

## Ключевые шаги
- Определить DTO для запроса (содержащего JSON Schema) и POJO для ответа (например, `GeneratedData.java`).
- Создать `AiGenerationService`, который будет инкапсулировать логику работы с `OllamaChatClient`.
- Реализовать метод, который принимает JSON Schema, формирует промпт с использованием `PromptTemplate` и `BeanOutputParser`.
- Настроить `application.yml` для подключения к Ollama.
- Создать контроллер с эндпоинтом, который принимает запрос и вызывает сервис.

## Критерии приемки
- `ai-service` имеет эндпоинт, который принимает JSON Schema.
- Сервис отправляет запрос в локально запущенную модель Mistral.
- Эндпоинт возвращает валидный JSON, соответствующий POJO, без какого-либо "мусора".
