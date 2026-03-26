---
id: TASK-2024-137
title: "Задача 4.10.13 (Backend): Режим синтетической генерации данных (Mock Mode)"
status: done
priority: medium
type: feature
estimate: 8h
created: 2025-11-21
parents: [TASK-2024-072]
dependencies: [TASK-2024-073]
arch_refs: [ADR-0019]
---

## Описание
В фазе проектирования (Design Time) у пользователя может не быть настроенного `Environment` или доступа к БД. Однако ему необходимо сгенерировать структуру данных (`TestDataSet`) для валидации сценария.

Необходимо научить `DataResolverService` работать в режиме отсутствия подключения к БД.

## Техническое решение

1.  **Модификация `DataResolverService.resolve()`:**
    *   Если `environmentId` равен `null` (или не найден), не выбрасывать ошибку.
    *   Вместо выполнения SQL (`executeSqlResolution`), вызывать метод `generateSyntheticValue(spec)`.

2.  **Метод `generateSyntheticValue`:**
    *   Анализировать метаданные поля (имя, тип).
    *   **Стратегии:**
        *   `UUID`: Генерировать случайный UUID.
        *   `String`: Генерировать строку вида `mock_value_[random]`.
        *   `Integer/Number`: Генерировать случайное число.
        *   *Advanced:* Подключить библиотеку **JavaFaker** (DataFaker) для генерации красивых имен, email, адресов.

3.  **Маркировка:**
    *   Добавлять в метаданные созданного `TestDataSet` флаг `origin: "SYNTHETIC"`.

## Критерии приемки
- [ ] API генерации данных принимает запрос без `environmentId`.
- [ ] Возвращается полный JSON, где вместо пропусков стоят заглушки (UUID, строки).
- [ ] Система не пытается подключиться к БД.