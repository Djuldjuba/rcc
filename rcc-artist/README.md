# RCC-Artist

## Описание

`rcc-artist` — микросервис проекта Rococo, отвечающий за работу с художниками.  
Реализует REST API и gRPC API, выступает в роли OAuth 2.0 Resource Server.

Сервис хранит данные художников в собственной базе MySQL и предоставляет CRUD-операции + поиск с пагинацией.

## Архитектура

### Технологический стек

- **Spring Boot 3.x / 4.x**
- **Spring Security** — OAuth 2.0 Resource Server (JWT)
- **Spring Data JPA**
- **Flyway** — миграции БД
- **MySQL 8.0**
- **gRPC** (net.devh grpc-server-spring-boot-starter)
- **Protobuf**
- **Lombok**
- **H2** (для тестов)

### Структура модуля

```
rcc-artist/
├── src/main/java/io/student/rcc/artist/
│   ├── RccArtistApplication.java              # Точка входа
│   ├── config/
│   │   └── SecurityConfig.java                # Security + JWT
│   ├── controller/
│   │   └── ArtistController.java              # REST API
│   ├── data/
│   │   ├── entity/
│   │   │   └── ArtistEntity.java              # JPA Entity (+ конвертация photo)
│   │   └── repository/
│   │       └── ArtistRepository.java          # Spring Data JPA
│   ├── domain/
│   │   ├── ArtistResponse.java
│   │   ├── CreateArtistRequest.java
│   │   └── UpdateArtistRequest.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── mapper/
│   │   └── ArtistMapper.java                  # Entity ↔ DTO ↔ gRPC
│   ├── service/
│   │   ├── ArtistService.java                 # Бизнес-логика
│   │   └── ArtistGrpcService.java             # gRPC реализация
│   └── util/                                  # (пусто / удалён)
├── src/main/proto/
│   └── artist_service.proto                   # gRPC контракт
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__init_artist_table.sql
└── src/test/java/...
    ├── controller/ArtistControllerTest.java
    └── service/ArtistServiceTest.java
```

## Security конфигурация

### OAuth 2.0 Resource Server

`SecurityConfig` настраивает защиту эндпойнтов через JWT:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/artist/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/artist/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/artist/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/api/artist/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/artist/**").authenticated()
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

- `GET /api/artist`
- `GET /api/artist/page`
- `GET /api/artist/{id}`
- Actuator health/info

### Защищённые endpoints (требуют JWT)

- `POST /api/artist`
- `PUT /api/artist/{id}`
- `DELETE /api/artist/{id}`

### JWT валидация

1. Проверка подписи через JWKS (`jwk-set-uri`)
2. Валидация `iss` и `exp`
3. Извлечение `sub` (username)

## REST API

### Получение списка художников

```http
GET /api/artist
```

**Response:** массив `ArtistResponse`

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Иван Айвазовский",
    "biography": "Русский художник-маринист",
    "photo": "data:image/jpeg;base64,..."
  }
]
```

### Пагинированный список + поиск

```http
GET /api/artist/page?name={search}&page=0&size=20
```

**Response:** Spring `Page<ArtistResponse>`

### Получение по ID

```http
GET /api/artist/{id}
```

### Создание художника

```http
POST /api/artist
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "name": "Василий Кандинский",
  "biography": "Русский художник-абстракционист",
  "photo": "data:image/jpeg;base64,..."
}
```

**Response:** `201 Created` + `ArtistResponse`

### Обновление художника

```http
PUT /api/artist/{id}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "name": "Новое имя",
  "biography": "Новая биография",
  "photo": "data:image/jpeg;base64,..."
}
```

### Удаление художника

```http
DELETE /api/artist/{id}
Authorization: Bearer {jwt}
```

**Response:** `204 No Content`

## gRPC API

Порт: **9090**

Сервис определён в `artist_service.proto`:

| Метод            | Описание                          |
|------------------|-----------------------------------|
| `GetArtist`      | Получить художника по ID          |
| `GetAllArtists`  | Получить всех художников          |
| `AddArtist`      | Создать художника                 |
| `UpdateArtist`   | Обновить художника                |
| `DeleteArtist`   | Удалить художника                 |
| `SearchArtists`  | Поиск с пагинацией                |

Реализация: `ArtistGrpcService`.

## Сервисный слой

### ArtistService

| Метод | Описание |
|-------|----------|
| `getAllArtists()` | Все художники |
| `getArtistsPage(name, page, size)` | Пагинация + поиск по имени (ignore case) |
| `getArtistById(id)` | Поиск по UUID |
| `createArtist(request)` | Создание + проверка уникальности имени |
| `updateArtist(id, request)` | Обновление + проверка уникальности имени |
| `deleteArtist(id)` | Удаление |

**Особенности:**

- `@Transactional(readOnly = true)` на read-операциях
- Проверка уникальности `name` при create/update
- `ResourceNotFoundException` при отсутствии сущности
- Конвертация `photo` (String ↔ byte[]) выполняется внутри `ArtistEntity`

### ArtistEntity — работа с фото

Фото хранится в БД как `LONGBLOB` (`byte[]`).  
В REST/gRPC API оно передаётся как `String` (обычно data-URI / base64).

Конвертация реализована прямо в entity:

```java
public void setPhotoFromString(String photoStr) {
    this.photo = (photoStr == null) ? null : photoStr.getBytes(StandardCharsets.UTF_8);
}

public String getPhotoAsString() {
    return (this.photo == null) ? null : new String(this.photo, StandardCharsets.UTF_8);
}
```

Отдельные утилитные классы `BytesAsString` / `StringAsBytes` больше не используются.

## Обработка ошибок

### GlobalExceptionHandler

| Исключение | HTTP Status | Описание |
|------------|-------------|----------|
| `ResourceNotFoundException` | 404 | Ресурс не найден |
| `IllegalArgumentException` | 400 | Дубликат имени / некорректные данные |
| `MethodArgumentNotValidException` | 400 | Ошибки `@Valid` |
| `Exception` | 500 | Непредвиденные ошибки |

**Формат ответа:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Художник не найден с id: '11111111-...'",
  "timestamp": "2026-11-16 19:42:00"
}
```

## База данных

### Flyway миграция

`V1__init_artist_table.sql`:

```sql
CREATE TABLE IF NOT EXISTS artist
(
    id        BINARY(16)    UNIQUE NOT NULL,
    name      VARCHAR(255)  UNIQUE NOT NULL,
    biography VARCHAR(2000) NOT NULL,
    photo     LONGBLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_artist_name ON artist(name);
```

### Репозиторий

```java
public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {
    Page<ArtistEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<ArtistEntity> findByName(String name);
    boolean existsByName(String name);
}
```

## Запуск

### Требования

- Java 21
- Docker (MySQL)
- Gradle

### Локальный запуск

```bash
# 1. Поднять MySQL (из корневого localenv.sh или вручную)
# 2. Запустить сервис
./gradlew :rcc-artist:bootRun
```

Или через IDE — main class: `io.student.rcc.artist.RccArtistApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8086 |
| gRPC     | 9090 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты (H2, без Flyway)

### Проверка

```bash
# Публичный endpoint
curl http://localhost:8086/api/artist

# Создание (нужен JWT)
curl -X POST http://localhost:8086/api/artist \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{"name":"Тестовый художник","biography":"Биография","photo":null}'
```

## Тесты

- **ArtistControllerTest** — интеграционные тесты REST API (MockMvc + JWT + SQL-фикстуры)
- **ArtistServiceTest** — unit-тесты сервиса (Mockito)

Запуск:

```bash
./gradlew :rcc-artist:test
```
