---
id: TASK-2024-037
title: "Задача 2.2.4: Интеграция `orchestra-api` с `ai-service`"
status: backlog
priority: medium
type: task
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-033]
dependencies: [TASK-2024-036]
arch_refs: [ARCH-core-services]
audit_log:
  - {date: 2024-07-30, user: "@RoboticArchitect", action: "created with status backlog"}
---
## Описание
Заменить существующую заглушку в `orchestra-api` на реальный HTTP-вызов к новому `ai-service`.

## Ключевые шаги
- В `AiService.java` (`orchestra-api`) внедрить `RestTemplate` или `WebClient`.
- Изменить метод `generateSimpleData` (и/или создать новый, более сложный) для:
  a. Получения JSON Schema для нужного эндпоинта (потребуется логика из `ProtocolSpecService`).
  b. Формирования запроса к ai-service.
  c. Отправки запроса и получения сгенерированных данных.
- Добавить URL `ai-service` в конфигурацию `orchestra-api` (`application.properties`).

## Критерии приемки
- Вызов эндпоинта `/api/v1/ai/data/generate-simple` в `orchestra-api` успешно проксирует запрос в `ai-service` и возвращает сгенерированные данные.
- Заглушка полностью удалена.
