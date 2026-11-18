# RCC-Userdata

## Описание

`rcc-userdata` — микросервис проекта Rococo, отвечающий за работу с данными пользователей.  
Реализует REST API и gRPC API, выступает в роли OAuth 2.0 Resource Server.

Сервис хранит профиль пользователя (имя, фамилия, аватар) в собственной базе MySQL.  
Создание пользователя происходит асинхронно через Kafka-событие регистрации.  
При обновлении профиля публикуется событие в топик `user-updates`.

## Архитектура

### Технологический стек

- **Spring Boot 3.x / 4.x**
- **Spring Security** — OAuth 2.0 Resource Server (JWT)
- **Spring Data JPA**
- **Flyway** — миграции БД
- **MySQL 8.0**
- **Spring Kafka**
- **gRPC** (net.devh grpc-server-spring-boot-starter)
- **Protobuf**
- **Lombok**
- **H2** (для тестов)

### Структура модуля

```
rcc-userdata/
├── src/main/java/io/student/rcc/userdata/
│   ├── RccUserdataApplication.java              # Точка входа
│   ├── config/
│   │   ├── SecurityConfig.java                  # Security + JWT
│   │   ├── JwtServerInterceptor.java            # gRPC JWT interceptor
│   │   └── KafkaProducerConfig.java             # Kafka producer + topic
│   ├── controller/
│   │   └── UserController.java                  # REST API
│   ├── data/
│   │   ├── entity/
│   │   │   └── UserEntity.java                  # JPA Entity (+ конвертация avatar)
│   │   └── repository/
│   │       └── UserRepository.java              # Spring Data JPA
│   ├── domain/
│   │   ├── UpdateUserRequest.java
│   │   └── UserResponse.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── kafka/
│   │   ├── UserRegisteredEvent.java
│   │   ├── UserRegisteredKafkaListener.java     # Создание пользователя
│   │   ├── UserUpdatedEvent.java
│   │   └── UserUpdatedEventListener.java        # Публикация в Kafka
│   ├── mapper/
│   │   └── UserMapper.java                      # Entity ↔ DTO
│   └── service/
│       ├── UserService.java                     # Интерфейс
│       ├── impl/
│       │   └── UserServiceImpl.java             # Бизнес-логика
│       └── UserdataGrpcService.java             # gRPC реализация
├── src/main/proto/
│   └── userdata_service.proto                   # gRPC контракт
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   ├── application-test.yml
│   └── db/migration/
│       └── V1__init_user_table.sql
└── src/test/java/...
    ├── controller/UserControllerTest.java
    └── service/UserServiceTest.java
```

## Security конфигурация

### OAuth 2.0 Resource Server

`SecurityConfig` настраивает защиту всех эндпойнтов через JWT:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
        );
    return http.build();
}
```

Все REST-эндпойнты требуют валидный JWT.

### gRPC JWT Interceptor

`JwtServerInterceptor` (`@GrpcGlobalServerInterceptor`) проверяет заголовок `Authorization: Bearer ...`:

1. Извлекает и валидирует JWT через `JwtDecoder`
2. Берёт claim `sub` (username)
3. Кладёт username в gRPC `Context` (`USERNAME_KEY`)
4. При ошибке закрывает вызов со статусом `UNAUTHENTICATED`

### JWT валидация

1. Проверка подписи через JWKS (`issuer-uri`)
2. Валидация `iss` и `exp`
3. Извлечение `sub` (username)

## REST API

Базовый путь: `/api/user`  
Порт: **8090**

### Получение текущего пользователя

```http
GET /api/user
Authorization: Bearer {jwt}
```

**Response:** `UserResponse`

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "username": "alice",
  "firstname": "Alice",
  "lastname": "Ivanova",
  "avatar": "data:image/jpeg;base64,..."
}
```

Пользователь должен уже существовать в БД (создаётся через Kafka-событие регистрации).  
Если пользователь не найден — `404`.

### Обновление профиля

```http
POST /api/user
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "firstname": "Alice-New",
  "lastname": "Sidorova",
  "avatar": "data:image/jpeg;base64,..."
}
```

Поля опциональны: `null` / отсутствие поля означает «не менять».  
После успешного обновления публикуется событие `UserUpdatedEvent` в топик `user-updates`.

**Response:** `200 OK` + `UserResponse`

## gRPC API

Порт: **9095**

Сервис определён в `userdata_service.proto`:

| Метод            | Описание                          |
|------------------|-----------------------------------|
| `GetCurrentUser` | Получить текущего пользователя    |
| `UpdateUser`     | Обновить профиль текущего пользователя |

Реализация: `UserdataGrpcService`.  
Username берётся из JWT-контекста (`JwtServerInterceptor.USERNAME_KEY`).

## Сервисный слой

### UserService / UserServiceImpl

| Метод | Описание |
|-------|----------|
| `getCurrentUser(username)` | Поиск пользователя по username |
| `createNewUserIfNotPresent(username)` | Поиск пользователя (ожидается создание через Kafka) |
| `updateUser(username, request)` | Обновление профиля + публикация `UserUpdatedEvent` |

**Особенности:**

- `@Transactional(readOnly = true)` на read-операциях
- `ResourceNotFoundException` при отсутствии пользователя
- Конвертация `avatar` (String ↔ byte[]) выполняется внутри `UserEntity`
- После `updateUser` публикуется Spring Application Event, который после commit отправляется в Kafka

### UserEntity — работа с аватаром

Аватар хранится в БД как `LONGBLOB` (`byte[]`).  
В REST/gRPC API он передаётся как `String` (обычно data-URI / base64).

Конвертация реализована прямо в entity:

```java
public void setAvatarFromString(String avatarStr) {
    this.avatar = (avatarStr == null) ? null : avatarStr.getBytes(StandardCharsets.UTF_8);
}

public String getAvatarAsString() {
    return (this.avatar == null) ? null : new String(this.avatar, StandardCharsets.UTF_8);
}
```

## Kafka

### Входящие события (Consumer)

**Топик:** `users`  
**Группа:** `rcc-userdata`  
**Событие:** `UserRegisteredEvent`

```java
record UserRegisteredEvent(
    String userId,
    String username,
    String eventType,
    LocalDateTime eventDate
)
```

`UserRegisteredKafkaListener`:

- Создаёт пользователя с заданным `userId` и `username`
- Если пользователь с таким username уже есть, но с другим id — удаляет старую запись и создаёт новую
- Идемпотентен по `id`

### Исходящие события (Producer)

**Топик:** `user-updates` (10 партиций, 1 реплика)  
**Событие:** `UserUpdatedEvent`

Публикуется после успешного commit транзакции обновления профиля (`@TransactionalEventListener(phase = AFTER_COMMIT)`).

## Обработка ошибок

### GlobalExceptionHandler

| Исключение | HTTP Status | Описание |
|------------|-------------|----------|
| `ResourceNotFoundException` | 404 | Пользователь не найден |
| `IllegalArgumentException` | 400 | Некорректные данные |
| `MethodArgumentNotValidException` | 400 | Ошибки `@Valid` |
| `Exception` | 500 | Непредвиденные ошибки |

**Формат ответа:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Пользователь не найден с username: 'ghost'",
  "timestamp": "2026-11-17 19:30:00"
}
```

## База данных

### Flyway миграция

`V1__init_user_table.sql`:

```sql
CREATE TABLE IF NOT EXISTS `user`
(
    id        BINARY(16)   UNIQUE NOT NULL,
    username  VARCHAR(50)  UNIQUE NOT NULL,
    firstname VARCHAR(255),
    lastname  VARCHAR(255),
    avatar    LONGBLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_user_username ON `user`(username);
```

### Репозиторий

```java
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

## Запуск

### Требования

- Java 21
- Docker (MySQL + Kafka)
- Gradle

### Локальный запуск

```bash
# 1. Поднять MySQL и Kafka (из корневого localenv.sh или вручную)
# 2. Запустить сервис
./gradlew :rcc-userdata:bootRun
```

Или через IDE — main class: `io.student.rcc.userdata.RccUserdataApplication`

### Порты

| Протокол | Порт |
|----------|------|
| REST     | 8090 |
| gRPC     | 9095 |

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose
- `application-test.yml` — тесты (H2, без Flyway)

### Проверка

```bash
# Получение текущего пользователя (нужен JWT)
curl http://localhost:8090/api/user \
  -H "Authorization: Bearer YOUR_JWT"

# Обновление профиля
curl -X POST http://localhost:8090/api/user \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{"firstname":"Иван","lastname":"Петров","avatar":null}'
```

## Тесты

- **UserControllerTest** — интеграционные тесты REST API (MockMvc + JWT + SQL-фикстуры)
- **UserServiceTest** — unit-тесты сервиса (Mockito)

Запуск:

```bash
./gradlew :rcc-userdata:test
```
