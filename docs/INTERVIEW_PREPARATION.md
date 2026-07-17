# Подготовка к собеседованию по `orchestra-project`

## Рассказ о проекте

Orchestra — прототип платформы интеграционного тестирования процессов. Пользователь импортирует BPMN, PlantUML или спецификацию API, описывает сценарии и окружения, затем API сохраняет запуск и ставит его в RabbitMQ. Отдельный исполнитель захватывает аренду запуска, восстанавливает контекст и выполняет шаги через плагины HTTP, PostgreSQL и Kafka. Планировщик управляет зависимостями внутри набора, а сервис на Spring AI и Ollama помогает создавать сценарии и тестовые данные.

После технического аудита я сузил обещания проекта до честного локального однопользовательского режима и укрепил границы доверия: клиент больше не выбирает организацию, административный API выключен по умолчанию, исходящие HTTP/JDBC-адреса работают по разрешённым спискам, SQL параметризован и только для чтения по умолчанию, секреты вынесены из репозитория, контейнерные порты замкнуты на `127.0.0.1`, а обязательные проверки запускаются в GitHub Actions. Следующий уровень зрелости — настоящая аутентификация, таблица исходящих событий, остановка при потере аренды и сквозные проверки.

## 20 вероятных вопросов и краткие ответы

1. **Как разделён проект?**
   На предметный модуль, API, исполнитель и сервис моделей в общем Maven-проекте, плюс отдельный интерфейс React ([`services/pom.xml`](../services/pom.xml#L22)).

2. **Как создаётся и запускается отдельный тест?**
   API сохраняет `TestRun` в состоянии `QUEUED`, затем публикует UUID в RabbitMQ ([`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L60)).

3. **Зачем отделён исполнитель?**
   Долгие и потенциально нестабильные интеграционные шаги не удерживают HTTP-запрос; их можно масштабировать отдельно через очередь ([`JobListener.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/listener/JobListener.java#L20)).

4. **Как предотвращается двойное выполнение?**
   Исполнитель условным обновлением захватывает аренду и продлевает её сердцебиением ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L79), [`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L142)).

5. **Что ещё не так с арендой?**
   Нулевая строка обновления сердцебиения только журналируется; выполнение надо прерывать, иначе после истечения аренды возможен второй работник ([`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L161)).

6. **Как добавить новый протокол?**
   Реализовать контракт `ProtocolPlugin`, зарегистрировать компонент и определить поддерживаемый тип канала ([`ProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/ProtocolPlugin.java#L1)).

7. **Как закрыт риск доступа к внутренней сети?**
   HTTP-шаг допускает только точные разрешённые узлы и отклоняет локальные/частные адреса, если оператор явно их не разрешил ([`OutboundUrlPolicy.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/security/OutboundUrlPolicy.java#L31)).

8. **Как защищён SQL?**
   Шаблоны превращаются в именованные параметры, запросы ограничены временем и строками, изменения выключены, а узел PostgreSQL проверяется отдельно ([`DbProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/DbProtocolPlugin.java#L71), [`JdbcUrlPolicy.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/security/JdbcUrlPolicy.java#L22)).

9. **Почему разрешённый список по умолчанию пустой?**
   Это безопасный отказ: новая установка не должна иметь исходящий доступ до явного решения оператора ([`application.properties`](../services/orchestra-executor/src/main/resources/application.properties#L15)).

10. **Как сейчас устроена организация?**
    Это однопользовательский режим: UUID задаёт сервер, клиентские заголовки и токены не влияют на контекст ([`TenantContextInterceptor.java`](../services/orchestra-api/src/main/java/com/orchestra/api/interceptor/TenantContextInterceptor.java#L17)).

11. **Почему не оставили ручной разбор JWT?**
    Декодирование полезной нагрузки без криптографической проверки создаёт ложную безопасность. До внедрения Spring Security безопаснее честно не обещать многопользовательский режим.

12. **Как защищено административное API?**
    Оно не создаётся без явного параметра включения; при включении требует отдельный токен ([`AdminController.java`](../services/orchestra-api/src/main/java/com/orchestra/api/controller/AdminController.java#L20)). Для промышленной версии всё равно нужны роли OpenID Connect.

13. **Где транзакционный риск?**
    База и RabbitMQ не образуют общую транзакцию: возможна запись без сообщения или сообщение до видимой записи ([`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L86)). Решение — таблица исходящих событий.

14. **Как работают наборы сценариев?**
    Планировщик периодически проверяет зависимости и режим параллельности, меняет готовые запуски на `QUEUED`, а после транзакции отправляет их в очередь ([`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L66)).

15. **Где возможна гонка планировщика?**
    Несколько экземпляров без распределённой блокировки могут одновременно обработать один набор ([`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L42)).

16. **Что делает сервис моделей?**
    Через Spring AI и Ollama он анализирует сценарии, строит данные и использует pgvector для смыслового поиска ([`DataPlannerAgent.java`](../services/ai-service/src/main/java/com/orchestra/ai/agent/DataPlannerAgent.java#L44)).

17. **Как ограничена модельная часть?**
    В основном Compose сервис не имеет узлового порта и доступен только API внутри сети контейнеров ([`docker-compose.yml`](../infra/docker-compose.yml#L135)). Это сетевое ограничение, а не замена служебной аутентификации.

18. **Какие проверки есть?**
    Есть интеграционная изоляция организации, модульные проверки AI и разрешения данных, политики HTTP/JDBC и проверки PlantUML. GitHub Actions поднимает PostgreSQL/pgvector и выполняет `mvn verify`, статический анализ, Vitest, сборку и проверку Compose ([`ci.yml`](../.github/workflows/ci.yml#L11)).

19. **Что показала проверка производительности?**
    Добавлены пределы HTTP и JDBC, но большой HTTP-ответ пока может быть полностью разобран в память, а основной пакет интерфейса превышает 500 КБ. Следующие меры — потоковый предел ответа и разделение кода.

20. **Готов ли проект к промышленной эксплуатации?**
    Нет. Он подходит для локальной демонстрации архитектуры. До эксплуатации нужны аутентификация и права, исходящие события, более строгая координация работников, служебная защита AI и сквозные проверки ([`PROJECT_AUDIT.md`](PROJECT_AUDIT.md#L21)).

## Слабые места проекта

- Нет пользовательской аутентификации и ролевой модели; безопасен только локальный однопользовательский режим.
- Публикация RabbitMQ не атомарна с фиксацией БД.
- Потеря аренды не останавливает уже выполняющийся шаг.
- Планировщик наборов не координируется между экземплярами.
- HTTP-ответ не ограничивается по размеру до полного разбора.
- Kafka-плагин и сервис моделей покрыты тестами слабо.
- Нет полноценной сквозной проверки поднятого окружения.
- Удалённый `.env` остаётся в истории Git, поэтому прежние значения надо заменить.
- Не выбрана лицензия для публичного повторного использования.

## Файлы, которые нужно изучить перед собеседованием

1. [`README.md`](../README.md#L1) — позиционирование и честные ограничения.
2. [`TenantContextInterceptor.java`](../services/orchestra-api/src/main/java/com/orchestra/api/interceptor/TenantContextInterceptor.java#L1) — текущая граница организации.
3. [`TenantAspect.java`](../services/orchestra-api/src/main/java/com/orchestra/api/aspect/TenantAspect.java#L1) — установка контекста RLS.
4. [`TestRunService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/TestRunService.java#L1) — создание запуска и транзакционный риск.
5. [`SuiteRunOrchestrator.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/SuiteRunOrchestrator.java#L1) — зависимости наборов.
6. [`TestRunExecutorService.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/service/TestRunExecutorService.java#L1) — аренда, возобновление и выполнение шагов.
7. [`OutboundUrlPolicy.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/security/OutboundUrlPolicy.java#L1) — защита исходящих HTTP-вызовов.
8. [`DbProtocolPlugin.java`](../services/orchestra-executor/src/main/java/com/orchestra/executor/plugin/impl/DbProtocolPlugin.java#L1) — безопасное выполнение SQL.
9. [`DataResolverService.java`](../services/orchestra-api/src/main/java/com/orchestra/api/service/DataResolverService.java#L159) — смысловой поиск и параметризованное разрешение данных.
10. [`DataPlannerAgent.java`](../services/ai-service/src/main/java/com/orchestra/ai/agent/DataPlannerAgent.java#L1) — поток генерации.
11. [`docker-compose.yml`](../infra/docker-compose.yml#L1) — роли, секреты и сетевые границы.
12. [`ci.yml`](../.github/workflows/ci.yml#L1) — воспроизводимые проверки.

## Формулировка для собеседования

«Я построил расширяемый прототип оркестрации интеграционных сценариев с очередью, арендой выполнения, общим контекстом и протокольными плагинами. При аудите я нашёл, что главные риски находились на границах доверия, а не в предметной модели. Я убрал подделываемый контекст пользователя, ввёл безопасный отказ для исходящих HTTP и JDBC, параметризовал SQL, разделил роли БД, закрыл порты и сделал проверки обязательными. Я также могу объяснить, почему текущая версия остаётся локальным прототипом и какие архитектурные шаги нужны до промышленной эксплуатации».
