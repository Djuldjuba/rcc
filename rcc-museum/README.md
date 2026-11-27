# RCC-Museum

## Описание

`rcc-museum` — микросервис проекта Rococo, отвечающий за работу с музеями.  
Реализует REST API и gRPC API, выступает в роли OAuth 2.0 Resource Server.

Сервис хранит данные музеев в собственной базе MySQL и обогащает ответы названием страны,  
получая его по gRPC из микросервиса `rcc-geo`.

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
rcc-museum/
├── src/main/java/io/student/rcc/museum/
│   ├── RccMuseumApplication.java              # Точка входа
│   ├── client/
│   │   └── CountryGrpcClient.java             # gRPC-клиент к rcc-geo
│   ├── config/
│   │   └── SecurityConfig.java                # Security + JWT
│   ├── controller/
│   │   └── MuseumController.java              # REST API
│   ├── data/
│   │   ├── entity/
│   │   │   └── MuseumEntity.java              # JPA Entity
│   │   └── repository/
│   │       └── MuseumRepository.java
│   ├── domain/
│   │   ├── MuseumResponse.java
│   │   ├── CreateMuseumRequest.java
│   │   └── UpdateMuseumRequest.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── mapper/
│   │   └── MuseumMapper.java                  # Entity ↔ DTO ↔ gRPC
│   ├── service/
│   │   ├── MuseumService.java                 # Бизнес-логика + кэш стран
│   │   └── MuseumGrpcService.java             # gRPC реализация
│   └── util/
│       ├── BytesAsString.java
│       └── StringAsBytes.java
├── src/main/proto/
│   ├── museum_service.proto                   # gRPC контракт музеев
│   └── country.proto                          # (копия контракта geo для клиента)
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__init_museum_table.sql
└── src/test/java/...
    ├── controller/MuseumControllerTest.java
    └── service/MuseumServiceTest.java
```

## Security конфигурация

### OAuth 2.0 Resource Server

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/museum/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/museum/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/museum/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/api/museum/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/museum/**").authenticated()
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

- `GET /api/museum`
- `GET /api/museum/page`
- `GET /api/museum/{id}`
- `GET /api/museum/country/{countryId}`
- Actuator health/info

### Защищённые endpoints (требуют JWT)

- `POST /api/museum`
- `PUT /api/museum/{id}`
- `DELETE /api/museum/{id}`

## REST API

### Получение списка музеев

```http
GET /api/museum
```

**Response:** массив `MuseumResponse`

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "title": "Эрмитаж",
    "description": "Крупнейший музей России",
    "photo": "data:image/jpeg;base64,...",
    "city": "Санкт-Петербург",
    "countryId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "countryName": "Россия"
  }
]
```

### Пагинированный список + поиск по названию

```http
GET /api/museum/page?title={search}&page=0&size=20
```

### Получение музея по ID

```http
GET /api/museum/{id}
```

### Музеи по стране

```http
GET /api/museum/country/{countryId}?page=0&size=20
```

### Создание музея

```http
POST /api/museum
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "title": "Третьяковская галерея",
  "description": "Музей русского искусства",
  "photo": "data:image/jpeg;base64,...",
  "geo": {
    "city": "Москва",
    "country": {
      "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    }
  }
}
```

**Response:** `201 Created` + `MuseumResponse`

### Обновление музея

```http
PUT /api/museum/{id}
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "title": "Эрмитаж (обновлён)",
  "description": "Новое описание",
  "photo": null,
  "geo": {
    "city": "Санкт-Петербург",
    "country": {
      "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    }
  }
}
```

### Удаление музея

```http
DELETE /api/museum/{id}
Authorization: Bearer {jwt}
```

**Response:** `204 No Content`

## gRPC API

Порт: **9093**

Сервис определён в `museum_service.proto`:

| Метод                  | Описание                          |
|------------------------|-----------------------------------|
| `GetMuseum`            | Получить музей по ID              |
| `GetAllMuseums`        | Получить все музеи                |
| `CreateMuseum`         | Создать музей                     |
| `UpdateMuseum`         | Обновить музей                    |
| `DeleteMuseum`         | Удалить музей                     |
| `SearchMuseums`        | Поиск с пагинацией                |
| `GetMuseumsByCountry`  | Музеи по стране                   |

Реализация: `MuseumGrpcService`.

## Интеграция с rcc-geo

Сервис использует **gRPC-клиент** (`CountryGrpcClient`) для получения названия страны по `countryId`.

```yaml
grpc:
  client:
    country:
      address: 'static://localhost:9094'   # локально
      # address: 'static://rcc-geo:9094'   # в Docker
      negotiation-type: plaintext
```

### Кэширование

В `MuseumService` реализован простой in-memory кэш:

```java
private final Map<String, String> countryNameCache = new ConcurrentHashMap<>();
```

При недоступности `rcc-geo` используется graceful degradation:  
возвращается `"Country {id}"`.

## Сервисный слой

### MuseumService

| Метод | Описание |
|-------|----------|
| `getAllMuseums()` | Все музеи |
| `getMuseumsPage(title, page, size)` | Пагинация + поиск по названию |
| `getMuseumById(id)` | Поиск по UUID |
| `getMuseumsByCountry(countryId, page, size)` | Музеи конкретной страны |
| `createMuseum(request)` | Создание |
| `updateMuseum(id, request)` | Обновление |
| `deleteMuseum(id)` | Удаление |

**Особенности:**

- `@Transactional` на write-операциях
- Название страны подтягивается через gRPC + кэш
- `ResourceNotFoundException` при отсутствии музея

### MuseumEntity

- `photo` хранится как `LONGBLOB` (`byte[]`)
- `countryId` — ссылка на страну в `rcc-geo` (без foreign key)

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
  "message": "Музей не найден с id: '...'",
  "timestamp": "2026-11-16 20:02:00"
}
```

## База данных

### Flyway миграция

`V1__init_museum_table.sql`:

```sql
CREATE TABLE IF NOT EXISTS museum
(
    id          BINARY(16)   UNIQUE NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    city        VARCHAR(255),
    photo       LONGBLOB,
    country_id  BINARY(16)   NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_museum_title ON museum(title);
CREATE INDEX idx_museum_country_id ON museum(country_id);
```

### Репозиторий

```java
public interface MuseumRepository extends JpaRepository<MuseumEntity, UUID> {
    Page<MuseumEntity> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<MuseumEntity> findAllByCountryId(UUID countryId, Pageable pageable);
}
```

## Запуск

### Требования

- Java 21
- Docker (MySQL + rcc-geo)
- Gradle

### Локальный запуск

```bash
# 1. Поднять MySQL и rcc-geo
# 2. Запустить сервис
./gradlew :rcc-museum:bootRun
```

Или через IDE — main class: `io.student.rcc.museum.RccMuseumApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8088 |
| gRPC     | 9093 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты (H2)

### Проверка

```bash
# Список музеев
curl http://localhost:8088/api/museum

# Поиск
curl "http://localhost:8088/api/museum/page?title=эрм"

# По стране
curl "http://localhost:8088/api/museum/country/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

# Создание (нужен JWT)
curl -X POST http://localhost:8088/api/museum \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Новый музей",
    "description": "Описание",
    "geo": {
      "city": "Москва",
      "country": { "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" }
    }
  }'
```

## Тесты

- **MuseumControllerTest** — интеграционные тесты REST API (MockMvc + JWT + SQL-фикстуры)
- **MuseumServiceTest** — unit-тесты сервиса (Mockito)

Запуск:

```bash
./gradlew :rcc-museum:test
```
