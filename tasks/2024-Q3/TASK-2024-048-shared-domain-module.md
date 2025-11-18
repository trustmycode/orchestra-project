---
id: TASK-2024-048
title: "Задача 3.1.1 (Backend): Создание общего модуля `orchestra-domain`"
status: backlog
priority: high
type: chore
estimate: 4h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-022]
arch_refs: [ARCH-core-services]
---
## Описание
Вынести общие классы (JPA-сущности, DTO, репозитории) из `orchestra-api` в отдельный Maven-модуль `orchestra-domain`, чтобы его могли использовать и `orchestra-api`, и `orchestra-executor` без дублирования кода.

## Ключевые шаги
1. Создать новый Maven-модуль `orchestra-domain`.
2. Перенести в него пакеты `com.orchestra.api.model`, `com.orchestra.api.dto`, `com.orchestra.api.repository`.
3. Добавить `orchestra-domain` как зависимость в `pom.xml` для `orchestra-api` и `orchestra-executor`.

## Критерии приемки
- Проекты `orchestra-api` и `orchestra-executor` успешно собираются с использованием общего модуля.
