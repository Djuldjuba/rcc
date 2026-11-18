# RCC-Gateway

## Описание

`rcc-gateway` — API Gateway проекта Rococo.  
Предоставляет единый REST API для фронтенда и проксирует запросы во внутренние микросервисы через gRPC.

Сервис выступает в роли OAuth 2.0 Resource Server: валидирует JWT и пробрасывает токен в gRPC-вызовы.

## Архитектура

### Технологический стек

- **Spring Boot 3.x / 4.x**
- **Spring Security** — OAuth 2.0 Resource Server (JWT)
- **gRPC Client** (net.devh grpc-client-spring-boot-starter)
- **Protobuf**
- **Lombok**
- **Jackson**

### Структура модуля

```
rcc-gateway/
├── src/main/java/io/student/rcc/gateway/
│   ├── GatewayApplication.java                 # Точка входа
│   ├── config/
│   │   ├── SecurityConfig.java                 # Security + CORS + JWT
│   │   └── JwtGrpcClientInterceptor.java       # Пробрасывание JWT в gRPC
│   ├── controller/
│   │   ├── ArtistController.java
│   │   ├── MuseumController.java
│   │   ├── PaintingController.java
│   │   ├── CountryController.java
│   │   ├── UserController.java
│   │   └── SessionController.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java         # Маппинг gRPC Status → HTTP
│   ├── model/
│   │   ├── ArtistJson.java
│   │   ├── MuseumJson.java
│   │   ├── PaintingJson.java
│   │   ├── CountryJson.java
│   │   ├── UserJson.java
│   │   ├── SessionJson.java
│   │   └── ErrorJson.java
│   └── service/
│       ├── ArtistClient.java
│       ├── MuseumClient.java
│       ├── PaintingClient.java
│       ├── CountryClient.java
│       └── UserClient.java
├── src/main/proto/                             # Клиентские proto-контракты
│   ├── artist_service.proto
│   ├── museum_service.proto
│   ├── painting_service.proto
│   ├── country.proto
│   └── userdata_service.proto
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   └── application-test.yml
└── src/test/java/...
```

## Security конфигурация

### OAuth 2.0 Resource Server

`SecurityConfig` настраивает:

- Публичные `GET`-эндпойнты для artist / museum / painting / country / session
- Все операции изменения и `/api/user/**` требуют JWT
- CORS с origin фронтенда (`rococo-front.base-uri`)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.GET, "/api/session").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/artist/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/museum/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/painting/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/country/**").permitAll()
    .requestMatchers("/api/user/**").authenticated()
    // POST / PUT / DELETE — authenticated
    .anyRequest().authenticated()
)
```

### JWT → gRPC

`JwtGrpcClientInterceptor` (`@GrpcGlobalClientInterceptor`):

1. Берёт JWT из `SecurityContextHolder`
2. Добавляет заголовок `Authorization: Bearer {token}` во все исходящие gRPC-вызовы

## REST API

Порт: **8085**

### Session

| Метод | Путь            | Auth | Описание                    |
|-------|-----------------|------|-----------------------------|
| GET   | `/api/session`  | —    | Информация о текущей сессии |

### Artist

| Метод  | Путь              | Auth | Описание                          |
|--------|-------------------|------|-----------------------------------|
| GET    | `/api/artist`     | —    | Пагинация + поиск по `name`       |
| GET    | `/api/artist/all` | —    | Все художники                     |
| GET    | `/api/artist/{id}`| —    | По ID                             |
| POST   | `/api/artist`     | JWT  | Создание                          |
| PUT    | `/api/artist/{id}`| JWT  | Полное обновление                 |
| PATCH  | `/api/artist`     | JWT  | Частичное обновление (id в теле)  |
| DELETE | `/api/artist/{id}`| JWT  | Удаление                          |

### Museum

| Метод  | Путь               | Auth | Описание                          |
|--------|--------------------|------|-----------------------------------|
| GET    | `/api/museum`      | —    | Пагинация + поиск по `title`      |
| GET    | `/api/museum/all`  | —    | Все музеи                         |
| GET    | `/api/museum/{id}` | —    | По ID                             |
| POST   | `/api/museum`      | JWT  | Создание                          |
| PUT    | `/api/museum/{id}` | JWT  | Полное обновление                 |
| PATCH  | `/api/museum`      | JWT  | Частичное обновление (id в теле)  |
| DELETE | `/api/museum/{id}` | JWT  | Удаление                          |

### Painting

| Метод  | Путь                          | Auth | Описание                     |
|--------|-------------------------------|------|------------------------------|
| GET    | `/api/painting`               | —    | Пагинация                    |
| GET    | `/api/painting/{id}`          | —    | По ID                        |
| GET    | `/api/painting/author/{id}`   | —    | Картины художника            |
| GET    | `/api/painting/museum/{id}`   | —    | Картины музея                |
| POST   | `/api/painting`               | JWT  | Создание                     |
| PUT    | `/api/painting/{id}`          | JWT  | Полное обновление            |
| PATCH  | `/api/painting`               | JWT  | Частичное обновление         |
| DELETE | `/api/painting/{id}`          | JWT  | Удаление                     |

### Country

| Метод | Путь                    | Auth | Описание                |
|-------|-------------------------|------|-------------------------|
| GET   | `/api/country`          | —    | Пагинация + фильтр      |
| GET   | `/api/country/{id}`     | —    | По ID                   |
| GET   | `/api/country/name/{name}` | — | По названию          |

### User

| Метод | Путь         | Auth | Описание                     |
|-------|--------------|------|------------------------------|
| GET   | `/api/user`  | JWT  | Текущий пользователь         |
| PATCH | `/api/user`  | JWT  | Обновление профиля           |

## gRPC-клиенты

Gateway не хранит данные — все операции делегируются микросервисам:

| Клиент           | gRPC-сервис          | Адрес (local)          |
|------------------|----------------------|------------------------|
| `ArtistClient`   | `artist-service`     | `localhost:9090`       |
| `MuseumClient`   | `museum-service`     | `localhost:9093`       |
| `PaintingClient` | `painting-service`   | `localhost:9091`       |
| `CountryClient`  | `geo-service`        | `localhost:9094`       |
| `UserClient`     | `userdata-service`   | `localhost:9095`       |

Каждый клиент:

- Маппит JSON-модели ↔ Protobuf
- Обрабатывает `StatusRuntimeException` (особенно `NOT_FOUND`)
- Для PATCH сначала читает текущее состояние, затем мержит поля

## Обработка ошибок

### GlobalExceptionHandler

Маппит gRPC-статусы в HTTP:

| gRPC Status          | HTTP Status |
|----------------------|-------------|
| `NOT_FOUND`          | 404         |
| `ALREADY_EXISTS`     | 409         |
| `INVALID_ARGUMENT`   | 400         |
| `UNAUTHENTICATED` / `PERMISSION_DENIED` | 403 |
| `UNAVAILABLE`        | 503         |
| остальные            | 500         |

**Формат ответа:**

```json
{
  "message": "Artist not found"
}
```

## Запуск

### Требования

- Java 21
- Запущенные downstream-сервисы (artist, museum, painting, geo, userdata)
- Auth-сервер (для JWT)
- Gradle

### Локальный запуск

```bash
./gradlew :rcc-gateway:bootRun
```

Или через IDE — main class: `io.student.rcc.gateway.GatewayApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8085 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты

### Проверка

```bash
# Публичный endpoint
curl http://localhost:8085/api/artist

# С JWT
curl http://localhost:8085/api/user \
  -H "Authorization: Bearer YOUR_JWT"
```

## Тесты

- **Controller-тесты** — MockMvc + Mockito (Artist, Museum, Painting, Country, User)
- **Client-тесты** — unit-тесты gRPC-клиентов (маппинг, обработка ошибок)

Запуск:

```bash
./gradlew :rcc-gateway:test
```
