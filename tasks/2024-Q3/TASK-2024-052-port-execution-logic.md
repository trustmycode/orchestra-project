---
id: TASK-2024-052
title: "Задача 3.1.5 (Backend): Перенос логики выполнения в `orchestra-executor`"
status: backlog
priority: high
type: task
estimate: 12h
created: 2024-07-30
updated: 2024-07-30
parents: [TASK-2024-022]
dependencies: [TASK-2024-051]
arch_refs: [ARCH-execution-engine, ARCH-core-services]
---
## Описание
Перенести и адаптировать логику выполнения шагов из `orchestra-api` в `TestRunExecutorService` в `orchestra-executor`.

## Ключевые шаги
1. Скопировать код, отвечающий за выполнение шагов, из `TestRunService` (`orchestra-api`) в `TestRunExecutorService` (`orchestra-executor`).
2. Адаптировать код для работы в новом сервисе (зависимости, конфигурация).
3. Реализовать логику обновления статуса `TestRun` на `PASSED` или `FAILED` после завершения всех шагов.
4. Провести рефакторинг для создания `ProtocolPlugin` SPI и вынести HTTP-логику в `HttpProtocolPlugin` (см. `TASK-2024-053`).

## Критерии приемки
- `orchestra-executor` полностью выполняет сценарий и корректно сохраняет все `TestStepResult`.
- Итоговый статус `TestRun` обновляется правильно.
