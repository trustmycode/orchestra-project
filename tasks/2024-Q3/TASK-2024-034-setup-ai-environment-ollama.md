---
id: TASK-2024-034
title: "Задача 2.2.1: Настройка локального окружения для AI (Ollama + GPT + Embeddings)"
status: backlog
priority: high
type: chore
estimate: 2h
created: 2024-07-30
updated: 2025-11-20
parents: [TASK-2024-033]
arch_refs: [ADR-0017, ADR-0031]
---
## Описание
Подготовить локальное окружение для работы с AI. Нам понадобятся две модели: основная LLM для генерации (GPT) и модель для создания векторных представлений (Embeddings) для работы RAG.

## Ключевые шаги
1.  Установить Ollama.
2.  **LLM (Chat):** Загрузить `ollama pull gpt-oss:20b` (или `qwen2.5-coder:14b` для слабых машин).
3.  **Embedding Model:** Загрузить **`ollama pull mxbai-embed-large`** (или `nomic-embed-text:1.5` для слабых машин). Это легкая и эффективная модель для векторизации текста, поддерживаемая Spring AI по умолчанию.
4.  Проверить доступность API.

## Критерии приемки
-   Команда `ollama list` показывает наличие `gpt-oss:20b` и `mxbai-embed-large`.
-   `ai-service` может подключиться к обеим моделям.
