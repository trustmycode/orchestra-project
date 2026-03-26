---
id: TASK-2024-156
title: "Задача: Настройка инфраструктуры i18n на фронтенде"
status: todo
priority: high
type: chore
estimate: 4h
created: 2025-11-22
parents: [TASK-2024-108]
---

## Описание

Внедрить библиотеку интернационализации в React-приложение для поддержки перевода интерфейса.

## Ключевые шаги

1.  Установить зависимости в `apps/orchestra-web`:

    ```bash
    npm install i18next react-i18next i18next-http-backend i18next-browser-languagedetector
    ```

2.  Создать файл конфигурации `src/lib/i18n.ts`:

    *   Настроить `fallbackLng: 'ru'`.

    *   Подключить загрузку JSON-файлов из `public/locales`.

3.  Подключить `i18n` в `src/index.tsx`.

4.  Создать базовый файл переводов `public/locales/ru/translation.json` (можно пока пустой или с тестовым ключом).

## Критерии приемки

- [ ] Приложение запускается без ошибок.

- [ ] Хук `useTranslation` доступен в компонентах.

- [ ] Текст подтягивается из JSON-файла.


