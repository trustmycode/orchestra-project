# Технический аудит `orchestra-project`

Дата повторной проверки: 17 июля 2026 года. Исходная точка: `121680b`. Документ описывает состояние после подготовки к безопасной публичной демонстрации.

## Итог

Orchestra — содержательный прототип платформы интеграционного тестирования с отдельными API, исполнителем, веб-интерфейсом и сервисом генерации. Первичный аудит выявил подделываемую организацию и административную роль, произвольные HTTP/JDBC-вызовы, строковую сборку SQL, отслеживаемый `.env`, чрезмерные права контейнеров и невоспроизводимые проверки. Эти блокирующие недостатки устранены или закрыты безопасными настройками по умолчанию.

Проект теперь приемлем для публичного портфолио как **локальный однопользовательский прототип**, но не готов к размещению в открытой сети: полноценной аутентификации пользователей пока нет, доставка сообщений не атомарна с транзакциями БД, а сквозное покрытие остаётся небольшим.

| Область | Оценка |
|---|---:|
| Архитектура | 68/100 |
| Надёжность | 56/100 |
| Поддерживаемость | 64/100 |
| Тесты | 55/100 |
| Безопасность | 58/100 |
| Инфраструктура | 75/100 |
| Документация | 78/100 |
| **Средняя** | **65/100** |

## Что исправлено перед публикацией

- Клиент больше не выбирает организацию через JWT или `X-Tenant-ID`: сервер всегда устанавливает настроенный UUID ([`TenantContextInterceptor.java`](../services/orchestra-api/src/main/java/com/orchestra/api/interceptor/TenantContextInterceptor.java#L17), [`TenantContextInterceptorTest.java`](../services/orchestra-api/src/test/java/com/orchestra/api/interceptor/TenantContextInterceptorTest.java#L24)).
- Административный контроллер отсутствует в приложении по умолчанию; при явном включении требует отдельный токен со сравнением за постоянное время ([`AdminController.java`](../services/orchestra-api/src/main/java/com/orchestra/api/controller/AdminController.java#L20), [`AdminController.java`](../services/orchestra-api/src/main/java/com/orchestra/api/controller/AdminController.java#L45)).
- HTTP-исполнитель требует точного разрешённого узла, отклоняет встроенные учётные данные и частные адреса, а клиент имеет пределы подключения и чтения ([`OutboundUrlPolicy.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/security/OutboundUrlPolicy.java#L31), [`HttpClientConfig.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/config/HttpClientConfig.java#L13)).
- JDBC-исполнитель принимает только разрешённые узлы PostgreSQL, использует именованные параметры, ограничивает время и строки, не сохраняет SQL и запрещает изменения по умолчанию ([`ConnectionManager.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/ConnectionManager.java#L23), [`DbProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/DbProtocolPlugin.java#L73), [`DbProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/DbProtocolPlugin.java#L89)).
- Разрешение тестовых данных также проверяет JDBC-узел, заменяет шаблоны именованными параметрами и допускает только один `SELECT` с пределом 1000 строк и 30 секунд ([`DatabaseAccessPolicy.java`](../services/orchestra-api/src/main/java/com/orchestra/api/security/DatabaseAccessPolicy.java#L25), [`DataResolverService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/DataResolverService.java#L209), [`DataResolverService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/DataResolverService.java#L224)).
- Текущий `.env` и архивные копии удалены, обязательные секреты больше не имеют рабочих значений по умолчанию, а прикладные сервисы используют отдельную роль БД ([`.env.example`](../infra/.env.example#L1), [`docker-compose.yml`](../infra/docker-compose.yml#L87), [`01-create-app-roles.sh`](../infra/postgres/init/01-create-app-roles.sh#L1)).
- Узловые порты привязаны к `127.0.0.1`, сервис моделей не публикуется наружу, MinIO больше не делает контейнер общедоступным ([`docker-compose.yml`](../infra/docker-compose.yml#L12), [`docker-compose.yml`](../infra/docker-compose.yml#L41), [`docker-compose.yml`](../infra/docker-compose.yml#L135)).
- Веб-интерфейс собирается воспроизводимо и раздаётся непривилегированным Nginx с защитными заголовками ([`Dockerfile`](../apps/orchestra-web/Dockerfile#L1), [`nginx.conf`](../apps/orchestra-web/nginx.conf#L7)).
- GitHub Actions проверяет Maven с PostgreSQL/pgvector, веб-часть и Docker Compose для каждого запроса на слияние ([`ci.yml`](../.github/workflows/ci.yml#L11), [`ci.yml`](../.github/workflows/ci.yml#L48), [`ci.yml`](../.github/workflows/ci.yml#L78)).

## 1. Назначение и стек

Система моделирует интеграционные сценарии и выполняет их через HTTP, PostgreSQL и Kafka. Сервер — Java 21, Spring Boot, JPA, Flyway, RabbitMQ, Spring AI, pgvector и Ollama ([`pom.xml`](../services/pom.xml#L7), [`pom.xml`](../services/pom.xml#L18)). Интерфейс использует React, TypeScript и Vite ([`package.json`](../apps/orchestra-web/package.json#L1)). Окружение объединено Docker Compose ([`docker-compose.yml`](../infra/docker-compose.yml#L1)).

## 2. Точки входа и основные компоненты

API запускается через [`OrchestraApiApplication.java`](../services/orchestra-api/src/main/java/com/orchestra/api/OrchestraApiApplication.java#L15). Получатель RabbitMQ передаёт работу в исполнитель ([`JobListener.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/listener/JobListener.java#L20)); протокольные границы оформлены плагинами HTTP, БД и Kafka ([`ProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/ProtocolPlugin.java#L1)). Сервис моделей принимает генерацию через [`GenerationController.java`](../services/ai-service/src/main/java/com/orchestra/ai/controller/GenerationController.java#L15).

## 3. Главный поток данных

API сохраняет запуск со статусом `QUEUED` и публикует его UUID в RabbitMQ ([`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L60), [`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L88)). Исполнитель захватывает аренду, восстанавливает контекст, пропускает уже сохранённые шаги, запускает плагин и фиксирует результат ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L79), [`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L106)). Наборы опрашиваются планировщиком раз в пять секунд ([`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L42)).

## 4. Архитектурные границы

Предметный модуль, API, исполнитель и сервис моделей разделены на Maven-модули ([`pom.xml`](../services/pom.xml#L22)). Это хорошая основа, но API и исполнитель используют общие JPA-репозитории и одну схему, поэтому граница между планированием и выполнением остаётся логической, а не изолированной. Текущий режим организации намеренно однопользовательский ([`TenantContextInterceptor.java`](../services/orchestra-api/src/main/java/com/orchestra/api/interceptor/TenantContextInterceptor.java#L28)).

## 5. Обработка ошибок

Общий обработчик не отдаёт трассировки неизвестных ошибок клиенту ([`GlobalExceptionHandler.java`](../services/orchestra-api/src/main/java/com/orchestra/api/exception/GlobalExceptionHandler.java#L23)). Исполнитель переводит запуск в `FAILED`, но поглощает исключение после записи состояния ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L127)); поэтому очередь не выполняет повтор большинства рабочих сбоев. Внешние плагины всё ещё включают текст исключения в обёртку, и его следует дополнительно нормализовать ([`HttpProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/HttpProtocolPlugin.java#L88)).

## 6. Асинхронность, фоновые задачи и гонки

Аренда запуска и сердцебиение уменьшают риск двойной работы ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L142)). Остаточный риск: потеря аренды только журналируется и не прерывает цикл шагов ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L161)). Планировщик наборов не имеет распределённой блокировки, поэтому несколько экземпляров могут одновременно выбрать один набор ([`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L42)).

## 7. Транзакции и база данных

Запись обычного запуска и публикация сообщения происходят в одном методе, но без таблицы исходящих событий; сообщение может уйти до фиксации транзакции ([`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L60), [`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L88)). Для наборов публикация вынесена после транзакции, но сбой между фиксацией и RabbitMQ оставит запись без сообщения ([`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L148)). Пулы внешних БД ограничены и закрываются при остановке, однако смена пароля профиля требует пересоздания процесса ([`ConnectionManager.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/ConnectionManager.java#L21)).

## 8. Безопасность и секреты

Текущие файлы не содержат заполненных секретов, а Compose требует их при запуске ([`.env.example`](../infra/.env.example#L1), [`docker-compose.yml`](../infra/docker-compose.yml#L7)). Однако старый `.env` существовал в истории Git: все прежние значения нужно считать раскрытыми и заменить. Полноценной пользовательской аутентификации нет, поэтому безопасная граница — только локальный интерфейс и сеть контейнеров ([`README.md`](../README.md#L18)). Статический административный токен приемлем лишь для локального показа, не как промышленная схема ([`AdminController.java`](../services/orchestra-api/src/main/java/com/orchestra/api/controller/AdminController.java#L28)).

## 9. Тесты и непокрытые сценарии

Добавлены проверки игнорирования клиентской организации, политик HTTP/JDBC, синтетического разрешения данных и кодирования PlantUML ([`TenantContextInterceptorTest.java`](../services/orchestra-api/src/test/java/com/orchestra/api/interceptor/TenantContextInterceptorTest.java#L24), [`DatabaseAccessPolicyTest.java`](../services/orchestra-api/src/test/java/com/orchestra/api/security/DatabaseAccessPolicyTest.java#L8), [`OutboundUrlPolicyTest.java`](../services/orchestra-executor/src/test/java/com/orchestra/executor/security/OutboundUrlPolicyTest.java#L8), [`plantuml.test.ts`](../apps/orchestra-web/src/lib/plantuml.test.ts#L1)). Критично не покрыты доставка RabbitMQ, потеря аренды, параллельный планировщик, реальные HTTP/JDBC-шаги, загрузка файлов, сервис моделей и сквозной пользовательский путь.

Локально успешно выполнены компиляция всех Maven-модулей, 10 модульных серверных проверок, 3 проверки Vitest, статический анализ TypeScript, производственная сборка Vite и оба варианта проверки Docker Compose. Полный `mvn verify` с PostgreSQL выполняется в GitHub Actions ([`ci.yml`](../.github/workflows/ci.yml#L15)).

## 10. Производительность

HTTP имеет пределы 5/30 секунд ([`HttpClientConfig.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/config/HttpClientConfig.java#L13)); JDBC ограничен 30 секундами и 1000 строками ([`DbProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/DbProtocolPlugin.java#L71)). Остались риски больших HTTP-ответов до их сохранения, последовательного выполнения шагов и создания Kafka-группы на каждый шаг ([`KafkaProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/KafkaProtocolPlugin.java#L66)). Сборка интерфейса предупреждает о главном пакете JavaScript больше 500 КБ; полезно ввести ленивую загрузку крупных редакторов.

## 11. Логи и наблюдаемость

Есть журналы стадий, идентификаторы запусков, сердцебиение и Spring Actuator в сервисе моделей. Заголовки и тело HTTP-запроса больше не сохраняются в структурированном результате ([`HttpProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/HttpProtocolPlugin.java#L65)). Не хватает сквозного идентификатора трассировки, показателей очереди, длительности плагинов, потерь аренды и централизованного редактирования чувствительных полей.

## 12. Docker, автоматизация и воспроизводимость

Секреты обязательны, сервисы ждут готовность зависимостей, наружные порты замкнуты на локальный интерфейс ([`docker-compose.yml`](../infra/docker-compose.yml#L17), [`docker-compose.yml`](../infra/docker-compose.yml#L85)). Веб-образ использует закреплённую версию Node.js и `npm ci` ([`Dockerfile`](../apps/orchestra-web/Dockerfile#L1)). Автоматизация полноценнее исходной, но пока проверяет конфигурацию, а не сборку всех контейнерных образов и не запускает сквозной стенд ([`ci.yml`](../.github/workflows/ci.yml#L78)).

## 13. Документация

Добавлены корневой запуск, явные ограничения безопасности, инструкция окружения, политика сообщения об уязвимостях и этот актуальный аудит ([`README.md`](../README.md#L1), [`SECURITY.md`](../SECURITY.md#L1), [`infra/README.md`](../infra/README.md#L1)). Архитектурные материалы полезны, но могут расходиться с фактическим однопользовательским режимом; главным источником истины следует считать текущий код и корневой README.

## 14. Часть AI/LLM

Сервис использует Spring AI, Ollama, память и pgvector ([`DataPlannerAgent.java`](../services/ai-service/src/main/java/com/orchestra/ai/agent/DataPlannerAgent.java#L44)). В основном Compose он доступен только внутренней сети ([`docker-compose.yml`](../infra/docker-compose.yml#L135)), что закрывает прямое внешнее изменение системных инструкций и знаний. Однако внутри сервиса нет собственной аутентификации, ограничений размера контекста/числа запросов и строгого разделения памяти; при будущем сетевом размещении эти меры обязательны ([`PromptController.java`](../services/ai-service/src/main/java/com/orchestra/ai/controller/PromptController.java#L17), [`KnowledgeBaseController.java`](../services/ai-service/src/main/java/com/orchestra/ai/controller/KnowledgeBaseController.java#L20)).

## 15. Риски публичной публикации

1. В истории Git остаётся удалённый `.env`; прежние значения необходимо заменить, а при подтверждённых настоящих секретах — отдельно очистить историю.
2. Файл `LICENSE` отсутствует, поэтому права повторного использования не определены ([`README.md`](../README.md#L99)).
3. Нельзя заявлять промышленную многопользовательскую безопасность: текущая версия честно однопользовательская.
4. Архитектурные риски исходящих событий, потери аренды и неполного покрытия следует оставить видимыми в портфолио, а не скрывать.

## Следующий план улучшений

1. Добавить OpenID Connect через Spring Security и серверное сопоставление пользователя с организацией и правами.
2. Реализовать таблицу исходящих событий и идемпотентного потребителя RabbitMQ.
3. Останавливать выполнение при потере аренды и блокировать планировщик между экземплярами.
4. Ограничить размер HTTP-ответа до разбора и добавить реальные проверки плагинов в изолированных контейнерах.
5. Изолировать сервис моделей отдельной служебной аутентификацией, квотами и организационными фильтрами.
6. Добавить сборку образов, сквозной запуск и проверку уязвимостей зависимостей в автоматизацию.
7. После решения владельца добавить `LICENSE`.
