# RCC-Painting

## Описание

`rcc-painting` — микросервис проекта Rococo, отвечающий за работу с картинами.  
Реализует REST API и gRPC API, выступает в роли OAuth 2.0 Resource Server.

Сервис хранит данные картин в собственной базе MySQL и обогащает ответы:
- именем художника (через gRPC → `rcc-artist`)
- названием музея (через gRPC → `rcc-museum`)

## Архитектура

### Технологический стек

- **Spring Boot 3.x / 4.x**
- **Spring Security** — OAuth 2.0 Resource Server (JWT)
- **Spring Data JPA**
- **Flyway** — миграции БД
- **MySQL 8.0**
- **gRPC** (server + client)
- **Protobuf**
- **Lombok**
- **H2** (для тестов)

### Структура модуля

```
rcc-painting/
├── src/main/java/io/student/rcc/painting/
│   ├── RccPaintingApplication.java            # Точка входа
│   ├── client/
│   │   ├── ArtistGrpcClient.java              # gRPC-клиент к rcc-artist
│   │   └── MuseumGrpcClient.java              # gRPC-клиент к rcc-museum
│   ├── config/
│   │   └── SecurityConfig.java                # Security + JWT
│   ├── controller/
│   │   └── PaintingController.java            # REST API
│   ├── data/
│   │   ├── entity/
│   │   │   └── PaintingEntity.java            # JPA Entity
│   │   └── repository/
│   │       └── PaintingRepository.java
│   ├── domain/
│   │   ├── PaintingResponse.java
│   │   ├── CreatePaintingRequest.java
│   │   └── UpdatePaintingRequest.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── mapper/
│   │   └── PaintingMapper.java                # Entity ↔ DTO ↔ gRPC
│   └── service/
│       ├── PaintingService.java               # Бизнес-логика + кэши
│       └── PaintingGrpcService.java           # gRPC реализация
├── src/main/proto/
│   ├── painting_service.proto                 # gRPC контракт картин
│   ├── artist_service.proto                   # (для клиента)
│   └── museum_service.proto                   # (для клиента)
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__init_painting_table.sql
└── src/test/java/...
    ├── controller/PaintingControllerTest.java
    └── service/PaintingServiceTest.java
```

## Security конфигурация

### OAuth 2.0 Resource Server

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/painting/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/painting/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/painting/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/api/painting/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/painting/**").authenticated()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
        );
    return http.build();
}
```

### Публичные endpoints

- `GET /api/painting`
- `GET /api/painting/page`
- `GET /api/painting/{id}`
- `GET /api/painting/artist/{artistId}`
- `GET /api/painting/museum/{museumId}`
- Actuator health/info

### Защищённые endpoints (требуют JWT)

- `POST /api/painting`
- `PUT /api/painting/{id}`
- `DELETE /api/painting/{id}`

## REST API

### Получение списка картин

```http
GET /api/painting
```

**Response:** массив `PaintingResponse`

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "title": "Над вечным покоем",
    "description": "Пейзаж Левитана",
    "content": "data:image/jpeg;base64,...",
    "artistId": "22222222-2222-2222-2222-222222222222",
    "artistName": "Исаак Левитан",
    "museumId": "33333333-3333-3333-3333-333333333333",
    "museumTitle": "Третьяковская галерея"
  }
]
```

### Пагинированный список + поиск по названию

```http
GET /api/painting/page?title={search}&page=0&size=20
```

### Получение картины по ID

```http
GET /api/painting/{id}
```

### Картины художника

```http
GET /api/painting/artist/{artistId}?page=0&size=20
```

### Картины музея

```http
GET /api/painting/museum/{museumId}?page=0&size=20
```

### Создание картины

```http
POST /api/painting
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "title": "Девятый вал",
  "description": "Картина Айвазовского",
  "content": "data:image/jpeg;base64,...",
  "artistId": "22222222-2222-2222-2222-222222222222",
  "museumId": "33333333-3333-3333-3333-333333333333"
}
```

**Response:** `201 Created` + `PaintingResponse`

### Обновление картины

```http
PUT /api/painting/{id}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "title": "Обновлённое название",
  "description": "Новое описание",
  "content": "data:image/jpeg;base64,...",
  "artistId": "22222222-2222-2222-2222-222222222222",
  "museumId": "33333333-3333-3333-3333-333333333333"
}
```

### Удаление картины

```http
DELETE /api/painting/{id}
Authorization: Bearer {jwt}
```

**Response:** `204 No Content`

## gRPC API

Порт: **9091**

Сервис определён в `painting_service.proto`:

| Метод                    | Описание                          |
|--------------------------|-----------------------------------|
| `GetPainting`            | Получить картину по ID            |
| `GetAllPaintings`        | Получить все картины              |
| `AddPainting`            | Создать картину                   |
| `UpdatePainting`         | Обновить картину                  |
| `DeletePainting`         | Удалить картину                   |
| `SearchPaintings`        | Поиск с пагинацией                |
| `GetPaintingsByArtist`   | Картины художника                 |
| `GetPaintingsByMuseum`   | Картины музея                     |

Реализация: `PaintingGrpcService`.

## Интеграция с другими сервисами

### gRPC-клиенты

```yaml
grpc:
  client:
    artist:
      address: 'static://localhost:9090'     # локально
      # address: 'static://rcc-artist:9090'  # Docker
      negotiation-type: plaintext
    museum:
      address: 'static://localhost:9093'
      # address: 'static://rcc-museum:9093'
      negotiation-type: plaintext
```

### Кэширование

В `PaintingService` реализованы два in-memory кэша:

```java
private final Map<String, String> artistNameCache = new ConcurrentHashMap<>();
private final Map<String, String> museumTitleCache = new ConcurrentHashMap<>();
```

При недоступности зависимых сервисов используется graceful degradation:
- `"Artist {id}"`
- `"Museum {id}"`

## Сервисный слой

### PaintingService

| Метод | Описание |
|-------|----------|
| `getAllPaintings()` | Все картины |
| `getPaintingsPage(title, page, size)` | Пагинация + поиск по названию |
| `getPaintingById(id)` | Поиск по UUID |
| `getPaintingsByArtist(artistId, page, size)` | Картины художника |
| `getPaintingsByMuseum(museumId, page, size)` | Картины музея |
| `addPainting(request)` | Создание |
| `updatePainting(id, request)` | Обновление |
| `deletePainting(id)` | Удаление |

**Особенности:**

- `@Transactional` на write-операциях
- Имя художника и название музея подтягиваются через gRPC + кэш
- `ResourceNotFoundException` при отсутствии картины

### PaintingEntity

- `content` хранится как `LONGBLOB` (`byte[]`)
- `artistId` — обязательная ссылка на художника
- `museumId` — опциональная ссылка на музей

## Обработка ошибок

### GlobalExceptionHandler

| Исключение | HTTP Status |
|------------|-------------|
| `ResourceNotFoundException` | 404 |
| `IllegalArgumentException` | 400 |
| `MethodArgumentNotValidException` | 400 |
| `Exception` | 500 |

**Формат ответа:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Картина не найден с id: '...'",
  "timestamp": "2026-11-16 20:03:00"
}
```

## База данных

### Flyway миграция

`V1__init_painting_table.sql`:

```sql
CREATE TABLE IF NOT EXISTS painting
(
    id          BINARY(16)   UNIQUE NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    content     LONGBLOB,
    artist_id   BINARY(16)   NOT NULL,
    museum_id   BINARY(16),
    PRIMARY KEY (id)
);

CREATE INDEX idx_painting_title ON painting(title);
CREATE INDEX idx_painting_artist_id ON painting(artist_id);
CREATE INDEX idx_painting_museum_id ON painting(museum_id);
```

### Репозиторий

```java
public interface PaintingRepository extends JpaRepository<PaintingEntity, UUID> {
    Page<PaintingEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<PaintingEntity> findAllByArtistId(UUID artistId, Pageable pageable);
    Page<PaintingEntity> findAllByMuseumId(UUID museumId, Pageable pageable);
}
```

## Запуск

### Требования

- Java 21
- Docker (MySQL + rcc-artist + rcc-museum)
- Gradle

### Локальный запуск

```bash
# 1. Поднять MySQL, rcc-artist и rcc-museum
# 2. Запустить сервис
./gradlew :rcc-painting:bootRun
```

Или через IDE — main class: `io.student.rcc.painting.RccPaintingApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8087 |
| gRPC     | 9091 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты (H2)

### Проверка

```bash
# Список картин
curl http://localhost:8087/api/painting

# Поиск
curl "http://localhost:8087/api/painting/page?title=вечн"

# По художнику
curl "http://localhost:8087/api/painting/artist/22222222-2222-2222-2222-222222222222"

# По музею
curl "http://localhost:8087/api/painting/museum/33333333-3333-3333-3333-333333333333"

# Создание (нужен JWT)
curl -X POST http://localhost:8087/api/painting \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Новая картина",
    "description": "Описание",
    "content": null,
    "artistId": "22222222-2222-2222-2222-222222222222",
    "museumId": "33333333-3333-3333-3333-333333333333"
  }'
```

## Тесты

- **PaintingControllerTest** — интеграционные тесты REST API (MockMvc + JWT + SQL-фикстуры)
- **PaintingServiceTest** — unit-тесты сервиса (Mockito)

Запуск:

```bash
./gradlew :rcc-painting:test
```
