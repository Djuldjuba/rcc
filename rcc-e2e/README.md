# RCC-E2E

## Описание

`rcc-e2e` — модуль end-to-end и интеграционных тестов проекта Rococo.  
Покрывает UI (Selenide), gRPC API и Kafka-сценарии.

Тесты работают с реальными микросервисами и БД (MySQL), поддерживают локальный и Docker-окружения.

## Архитектура

### Технологический стек

- **JUnit 5**
- **Selenide** — UI-тесты
- **Allure** — отчёты
- **gRPC** — API-тесты микросервисов
- **OkHttp / Retrofit** — REST (Auth)
- **Hibernate / Spring JDBC / pure JDBC** — доступ к БД
- **Atomikos** — XA-транзакции
- **P6Spy** — логирование SQL в Allure
- **Kafka Clients** — проверка событий
- **AShot** — screenshot comparison
- **JavaFaker** — генерация тестовых данных

### Структура модуля

```
rcc-e2e/
├── src/test/java/io/student/rococo/
│   ├── api/                          # gRPC и REST клиенты
│   │   ├── grpc/                     # ArtistGrpcApi, MuseumGrpcApi, ...
│   │   ├── grpc/core/                # GrpcChannels, GrpcStubs
│   │   └── core/                     # ThreadSafeCookieStore
│   ├── config/                       # LocalConfig / DockerConfig
│   ├── data/
│   │   ├── entity/                   # JPA-сущности (Artist, Museum, Painting, User, ...)
│   │   ├── repository/               # Интерфейсы + impl (Hibernate / JDBC / Spring JDBC)
│   │   ├── mapper/                   # EntityManagers, DataSources, ResultSetExtractor
│   │   └── logging/                  # Allure SQL attachments
│   ├── jupiter/
│   │   ├── annotation/               # @User, @Artist, @Museum, @Painting, @ApiLogin, ...
│   │   ├── extensions/               # UserExtension, ArtistExtension, BrowserExtension, ...
│   │   └── TestDataPresets.java      # Preset-данные (Левитан, Лувр, ...)
│   ├── kafka/                        # KafkaService, KafkaProducerClient
│   ├── model/                        # JSON-модели (ArtistJson, MuseumJson, ...)
│   ├── page/                         # Page Objects (MainPage, ArtistPage, ...)
│   │   └── component/                # Header, ModalForm, SearchField
│   ├── service/                      # Клиенты к БД (ArtistDbClient, MuseumDbClient, ...)
│   ├── test/
│   │   ├── ui/                       # UI-тесты
│   │   └── grpc/                     # gRPC-тесты
│   └── utils/                        # RandomDataUtils, OAuthUtils, Waiter, ScreenDiffResult
├── src/test/proto/                   # Proto-контракты для gRPC-клиентов
├── src/test/resources/
│   ├── files/                        # Фото и эталонные скриншоты
│   ├── META-INF/persistence.xml
│   ├── junit-platform.properties
│   ├── allure.properties
│   └── spy.properties
└── build.gradle
```

## Типы тестов

### UI-тесты (`test/ui`)

| Класс            | Описание                                      |
|------------------|-----------------------------------------------|
| `LoginTest`      | Логин, регистрация, ошибки валидации          |
| `MainPageTest`   | Тема, навигация через иконку Rococo           |
| `ArtistsTests`   | CRUD художников, поиск, карточка, скриншоты   |
| `PaintingsTests` | CRUD картин, поиск, валидация                 |
| `MuseumsTest`    | CRUD музеев, поиск, валидация                 |
| `ProfileTest`    | Редактирование профиля, аватар, logout        |

### gRPC-тесты (`test/grpc`)

| Класс               | Сервис          |
|---------------------|-----------------|
| `ArtistGrpcTest`    | rcc-artist      |
| `MuseumGrpcTest`    | rcc-museum      |
| `PaintingGrpcTest`  | rcc-painting    |
| `UserdataGrpcTest`  | rcc-userdata    |

### Kafka-тесты

| Класс                        | Описание                                      |
|------------------------------|-----------------------------------------------|
| `UserdataKafkaConsumerTest`  | `UserRegisteredEvent` → создание user в БД    |

## Аннотации и расширения

### Основные аннотации

| Аннотация        | Назначение                                              |
|------------------|---------------------------------------------------------|
| `@User`          | Создаёт пользователя (+ опционально artists/museums/paintings) |
| `@Artist`        | Создаёт художника (или preset: `levitan` / `louvre`)    |
| `@Museum`        | Создаёт музей                                           |
| `@Painting`      | Создаёт картину (требует artist + museum)               |
| `@ApiLogin`      | Логин через Auth API + установка JWT в браузер          |
| `@ApiLoginGrpc`  | Логин для gRPC-тестов (токен в параметр `@Token`)       |
| `@ScreenShotTest`| Screenshot comparison (AShot)                           |
| `@Token`         | Инжект JWT-токена в параметр теста                      |

### Пример

```java
@Test
@User(
    museums = @Museum(preset = "levitan"),
    artists = @Artist(preset = "levitan"),
    paintings = @Painting(preset = "levitan")
)
@ApiLogin
@ScreenShotTest(value = "files/paintings/nad_vechnym_pokoem_in_card.jpg")
@DisplayName("Проверка карточки картины")
void checkPaintingCard(TestData testData, BufferedImage expectedPainting) {
    // ...
}
```

### Suite-расширения (автоматически)

- `DatabaseExtension` — закрытие EntityManager / JDBC-соединений
- `BrowserExtension` — Selenide + Selenoid (Docker)
- `KafkaExtension` — фоновый consumer топика `users`
- `AllureSuiteExtension` — отправка результатов в Allure Docker Service
- `TestDataExtension` — cleanup созданных сущностей

## Доступ к данным

### Выбор реализации репозитория

```bash
-Drepository.impl=jpa          # Hibernate (по умолчанию)
-Drepository.impl=jdbc         # pure JDBC
-Drepository.impl=spring-jdbc  # Spring JdbcTemplate
```

### Config

- `LocalConfig` — `localhost`, порты сервисов
- `DockerConfig` — хосты Docker-сети (`rcc-mysql`, `rcc-kafka`, ...)

Выбор: `-Dtest.env=local` (default) / `-Dtest.env=docker`

## Page Objects

| Page            | Описание                          |
|-----------------|-----------------------------------|
| `MainPage`      | Главная, тема, навигация          |
| `LoginPage`     | Логин / регистрация               |
| `RegisterPage`  | Форма регистрации                 |
| `ArtistPage`    | Список и карточка художника       |
| `PaintingPage`  | Список и карточка картины         |
| `MuseumPage`    | Список и карточка музея           |
| `ProfilePage`   | Модалка профиля                   |

Компоненты: `Header`, `ModalForm`, `SearchField`.

## Запуск

### Требования

- Java 21
- Запущенный стек Rococo (auth, gateway, artist, museum, painting, userdata, geo, MySQL, Kafka)
- Chrome / Firefox (или Selenoid в Docker)

### Локально

```bash
./gradlew :rcc-e2e:test
```

С параметрами:

```bash
./gradlew :rcc-e2e:test \
  -Dtest.env=local \
  -Dbrowser=chrome \
  -Drepository.impl=jpa
```

### Docker

```bash
# В Dockerfile / CI
gradle :rcc-e2e:test --no-daemon \
  -Dtest.env=docker \
  -Duser.timezone=Europe/Moscow \
  -Dbrowser=${BROWSER:-chrome}
```

### Параллельность

Настроена в `junit-platform.properties`:

- 3 потока
- concurrent mode для классов и методов

## Allure

Результаты: `build/allure-results`

При наличии `ALLURE_DOCKER_API` — автоматическая отправка и генерация отчёта после suite.

SQL-запросы (через P6Spy) и HTTP (OkHttp) прикрепляются как Allure-attachments.

## Resources

```
src/test/resources/files/
├── artists/          # levitan.jpg, leonardo_da_vinci.jpg, эталоны скриншотов
├── museums/          # tretyakovka.jpg, louvre.jpg, ...
├── paintings/        # nad_vechnym_pokoem.jpg, mona_lisa.jpg, ...
├── profile/          # avatar.png, ...
└── mainPage/         # dark_mode.jpg, light_mode.jpg
```

Presets (`TestDataPresets`):

- **levitan** — Левитан / Третьяковка / «Над вечным покоем»
- **louvre** — Леонардо / Лувр / «Мона Лиза»
