# RCC-Geo

## Описание

`rcc-geo` — микросервис проекта Rococo, отвечающий за справочник стран.  
Предоставляет REST API и gRPC API, выступает в роли OAuth 2.0 Resource Server.

Сервис хранит список стран в собственной базе MySQL и поддерживает поиск с пагинацией.  
Все операции чтения публичные.

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
rcc-geo/
├── src/main/java/io/student/rcc/geo/
│   ├── GeoApplication.java                    # Точка входа
│   ├── config/
│   │   └── SecurityConfig.java                # Security + JWT
│   ├── controller/
│   │   └── CountryController.java             # REST API стран
│   ├── entity/
│   │   └── CountryEntity.java                 # JPA Entity
│   ├── exception/
│   │   └── CountryNotFoundException.java
│   ├── model/
│   │   ├── CountryJson.java                   # DTO страны
│   │   └── GeoJson.java                       # DTO гео (city + country)
│   ├── repository/
│   │   └── CountryRepository.java
│   └── service/
│       ├── CountryService.java                # Бизнес-логика
│       └── CountryGrpcService.java            # gRPC реализация
├── src/main/proto/
│   └── country.proto                          # gRPC контракт
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__init_geo_schema.sql
└── src/test/java/...
    ├── controller/CountryControllerTest.java
    └── service/CountryServiceTest.java
```

## Security конфигурация

### OAuth 2.0 Resource Server

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/country/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/country/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/country/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/api/country/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/country/**").authenticated()
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

- `GET /api/country`
- `GET /api/country/{id}`
- `GET /api/country/name/{name}`
- Actuator health/info

### Защищённые endpoints

На данный момент write-операций нет, но конфигурация уже готова к их появлению.

## REST API

### Получение списка стран (с пагинацией и поиском)

```http
GET /api/country?title={search}&page=0&size=20
```

Параметр `title` — опциональный поиск по имени (ignore case).

**Response:** Spring `Page<CountryJson>`

```json
{
  "content": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "name": "Россия"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Получение страны по ID

```http
GET /api/country/{id}
```

**Response:**

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "name": "Россия"
}
```

### Получение страны по имени

```http
GET /api/country/name/{name}
```

Пример:

```http
GET /api/country/name/Япония
```

## gRPC API

Порт: **9094**

Сервис определён в `country.proto`:

| Метод              | Описание                          |
|--------------------|-----------------------------------|
| `GetAllCountries`  | Список стран с пагинацией и поиском |
| `GetCountryById`   | Получить страну по ID             |
| `GetCountryByName` | Получить страну по имени          |

Реализация: `CountryGrpcService`.

## Сервисный слой

### CountryService

| Метод | Описание |
|-------|----------|
| `all(name, pageable)` | Все страны / поиск по имени (ignore case) + пагинация |
| `findCountryById(id)` | Поиск по UUID |
| `findCountryByName(name)` | Поиск по точному имени |

**Особенности:**

- Все методы `@Transactional(readOnly = true)`
- При отсутствии страны выбрасывается `CountryNotFoundException` → 404

### CountryJson

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CountryJson(UUID id, String name) {
    public static CountryJson fromEntity(CountryEntity entity) { ... }
    public CountryEntity toEntity() { ... }
}
```

## Обработка ошибок

### CountryNotFoundException

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CountryNotFoundException extends RuntimeException { ... }
```

Примеры сообщений:

- `Страна не найдена по id: ...`
- `Страна не найдена по имени: ...`

## База данных

### Flyway миграция

`V1__init_geo_schema.sql`:

- Создаёт таблицу `country`
- Добавляет индекс по `name`
- Заполняет справочник большим количеством стран мира

```sql
CREATE TABLE IF NOT EXISTS `country`
(
    id   BINARY(16)   UNIQUE NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_country_name ON country(name);
```

### Репозиторий

```java
public interface CountryRepository extends JpaRepository<CountryEntity, UUID> {
    Page<CountryEntity> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
    Optional<CountryEntity> findByName(String name);
}
```

## Запуск

### Требования

- Java 21
- Docker (MySQL)
- Gradle

### Локальный запуск

```bash
# 1. Поднять MySQL
# 2. Запустить сервис
./gradlew :rcc-geo:bootRun
```

Или через IDE — main class: `io.student.rcc.geo.GeoApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8091 |
| gRPC     | 9094 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты (H2, без Flyway)

### Проверка

```bash
# Список стран
curl "http://localhost:8091/api/country?page=0&size=5"

# Поиск
curl "http://localhost:8091/api/country?title=росс"

# По ID
curl http://localhost:8091/api/country/11111111-1111-1111-1111-111111111111

# По имени
curl http://localhost:8091/api/country/name/Япония
```

## Тесты

- **CountryControllerTest** — интеграционные тесты REST API (MockMvc + SQL-фикстуры)
- **CountryServiceTest** — unit-тесты сервиса (Mockito)

Запуск:

```bash
./gradlew :rcc-geo:test
```
