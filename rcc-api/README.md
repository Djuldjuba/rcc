# RCC-API - Resource Server

## Описание

`rcc-api`- монолитный бэкенд с REST API проекта Rococo, реализующий OAuth 2.0 Resource Server. Модуль предоставляет API
для работы с художниками, картинами, музеями, странами и пользователями.
Поддерживает как реальную работу с базой данных User, Museum, так и mock-объекты (Paintings, Artists).

## Архитектура

### Технологический стек

- **Spring Boot 4.0.2**
- **Spring Security 7.x** - OAuth 2.0 Resource Server
- **Spring Data JPA** - работа с базой данных
- **Flyway** - миграции БД
- **MySQL 8.0** - СУБД

### Структура модуля

```
rcc-api/
├── src/main/java/io/student/rococo/
│   ├── RococoApiApplication.java           # Точка входа
│   ├── config/
│   │   └── RococoApiConfiguration.java     # Security конфигурация
│   ├── controller/
│   │   ├── ArtistMockController.java       # Mock API для художников
│   │   ├── PaintingMockController.java     # Mock API для картин
│   │   ├── CountryController.java          # API стран
│   │   ├── MuseumController.java           # API музеев
│   │   ├── UserController.java             # API пользователей
│   │   └── SessionController.java          # API сессии
│   ├── data/
│   │   ├── entity/
│   │   │   ├── CountryEntity.java          # Entity страны
│   │   │   ├── MuseumEntity.java           # Entity музея
│   │   │   └── UserEntity.java             # Entity пользователя
│   │   └── repository/
│   │       ├── CountryRepository.java      # JPA репозиторий стран
│   │       ├── MuseumRepository.java       # JPA репозиторий музеев
│   │       └── UserRepository.java         # JPA репозиторий пользователей
│   ├── exception/
│   │   └── ResourceNotFoundException.java  # Custom exception
│   ├── model/
│   │   ├── ArtistJson.java                 # DTO художника
│   │   ├── PaintingJson.java               # DTO картины
│   │   ├── CountryJson.java                # DTO страны
│   │   ├── MuseumJson.java                 # DTO музея
│   │   ├── UserJson.java                   # DTO пользователя
│   │   ├── GeoJson.java                    # DTO гео
│   │   ├── SessionJson.java                # DTO сессии
│   │   └── ErrorJson.java                  # DTO ошибки
│   ├── service/
│   │   ├── api/
│   │   │   ├── CountryService.java         # Интерфейс сервиса стран
│   │   │   ├── MuseumService.java          # Интерфейс сервиса музеев
│   │   │   └── UserService.java            # Интерфейс сервиса пользователей
│   │   ├── impl/
│   │   │   ├── CountryServiceImpl.java     # Реализация сервиса стран
│   │   │   ├── MuseumServiceImpl.java      # Реализация сервиса музеев
│   │   │   └── UserServiceImpl.java        # Реализация сервиса пользователей
│   │   ├── GlobalExceptionHandler.java     # Глобальная обработка ошибок
│   │   └── cors/
│   │       └── CorsCustomizer.java         # CORS настройки
│   └── util/
│       ├── BytesAsString.java              # Конвертер byte[] -> String
│       └── StringAsBytes.java              # Конвертер String -> byte[]
└── src/main/resources/
    ├── application.yml                      # Конфигурация приложения
    ├── db/migration/                        # Flyway миграции
    └── mock/                                # Mock данные (JSON)
        ├── artist/                          # JSON файлы художников
        └── painting/                        # JSON файлы картин
```

## Security конфигурация

### OAuth 2.0 Resource Server

**RococoApiConfiguration** настраивает защиту эндпойнтов через JWT токены:

```java

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  http.csrf(AbstractHttpConfigurer::disable)  // CSRF отключен для REST API
      .authorizeHttpRequests(customizer ->
          customizer
              .requestMatchers(GET, "/api/session").permitAll()
              .requestMatchers(GET, "/api/artist/**").permitAll()
              .requestMatchers(GET, "/api/museum/**").permitAll()
              .requestMatchers(GET, "/api/painting/**").permitAll()
              .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
  return http.build();
}
```

### Публичные endpoints (без аутентификации)

- `GET /api/session` - получение информации о сессии
- `GET /api/artist/**` - чтение художников
- `GET /api/museum/**` - чтение музеев
- `GET /api/painting/**` - чтение картин
- `GET /api/country/**` - чтение стран

### Защищенные endpoints (требуют JWT токен)

- `PATCH /api/user` - обновление профиля пользователя
- `POST /api/museum` - создание музея
- `PATCH /api/museum` - обновление музея
- Все остальные POST/PUT/PATCH/DELETE операции

### JWT Token валидация

1. Проверка подписи токена через JWKS endpoint Authorization Server
2. Валидация `iss` (issuer) - должен совпадать с `issuer-uri`
3. Валидация `exp` (expiration) - токен не должен быть просрочен
4. Извлечение `sub` (subject) - username пользователя

## API Endpoints

### Artists API

#### Mock контроллер

```http
GET /api/artist?name={search}&page={page}&size={size}
```

Получение списка художников с пагинацией и поиском

**Response:**

```json
{
  "content": [
    {
      "id": "5a486b2f-c361-459e-bd3f-60692a635ea9",
      "name": "Левитан",
      "biography": "Исаак Ильич Левитан — русский живописец...",
      "photo": "data:image/jpeg;base64,..."
    }
  ],
  "pageable": {
    ...
  },
  "totalElements": 3,
  "totalPages": 1
}
```

```http
GET /api/artist/{id}
```

Получение художника по ID

```http
PATCH /api/artist
```

Обновление художника (mock - просто возвращает переданные данные)

```http
POST /api/artist
```

Создание художника (mock - просто возвращает переданные данные)

**Mock данные:** `src/main/resources/mock/artist/*.json`

### Paintings API

#### Mock контроллер

```http
GET /api/painting?title={search}&page={page}&size={size}
```

Получение списка картин с пагинацией и поиском

```http
GET /api/painting/{id}
```

Получение картины по ID

```http
GET /api/painting/author/{authorId}?page={page}&size={size}
```

Получение картин по ID автора

```http
PATCH /api/painting
```

Обновление картины (mock)

```http
POST /api/painting
```

Создание картины (mock)

**Mock данные:** `src/main/resources/mock/painting/*.json`

### Museums API

```http
GET /api/museum?title={search}&page={page}&size={size}
```

Получение списка музеев с пагинацией и поиском

**Response:**

```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "title": "Эрмитаж",
      "description": "Государственный Эрмитаж...",
      "photo": "data:image/jpeg;base64,...",
      "geo": {
        "city": "Санкт-Петербург",
        "country": {
          "id": "...",
          "name": "Россия"
        }
      }
    }
  ],
  "totalElements": 10
}
```

```http
GET /api/museum/{id}
```

Получение музея по ID

```http
POST /api/museum
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Новый музей",
  "description": "Описание",
  "photo": "data:image/jpeg;base64,...",
  "geo": {
    "city": "Москва",
    "country": {
      "id": "uuid",
      "name": "Россия"
    }
  }
}
```

Создание музея (требует аутентификацию)

```http
PATCH /api/museum
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "id": "uuid",
  "title": "Обновленное название",
  ...
}
```

Обновление музея (требует аутентификацию)

### Countries API

```http
GET /api/country?title={search}&page={page}&size={size}
```

Получение списка стран с пагинацией и поиском

```http
GET /api/country/{id}
```

Получение страны по ID

```http
GET /api/country/name/{name}
```

Получение страны по имени

### Users API

```http
GET /api/user
Authorization: Bearer {jwt_token}
```

Получение текущего пользователя

**Response:**

```json
{
  "id": "uuid",
  "username": "user@example.com",
  "firstname": "Иван",
  "lastname": "Иванов",
  "avatar": "data:image/jpeg;base64,..."
}
```

```http
PATCH /api/user
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "username": "user@example.com",
  "firstname": "Иван",
  "lastname": "Петров",
  "avatar": "data:image/jpeg;base64,..."
}
```

Обновление профиля пользователя

**Валидация:**

- `username` нельзя изменить (проверяется через JWT token)

### Session API

```http
GET /api/session
```

Получение информации о текущей сессии (публичный endpoint)

**Response (аутентифицирован):**

```json
{
  "username": "user@example.com",
  "issuedAt": "2024-02-06T00:00:00.000+00:00",
  "expiresAt": "2024-02-06T01:00:00.000+00:00"
}
```

**Response (не аутентифицирован):**

```json
{
  "username": null,
  "issuedAt": null,
  "expiresAt": null
}
```

## Сервисный слой

### CountryService

**Методы:**

- `Page<CountryJson> all(String name, Pageable pageable)` - получение всех стран с фильтрацией
- `CountryJson findCountryById(String id)` - поиск по ID
- `CountryJson findCountryByName(String name)` - поиск по имени

**Особенности:**

- `@Transactional(readOnly = true)` для read операций
- Использование `Optional.orElseThrow()` с `ResourceNotFoundException`

### MuseumService

**Методы:**

- `Page<MuseumJson> all(String title, Pageable pageable)` - получение всех музеев
- `MuseumJson findById(String id)` - поиск по ID
- `MuseumJson update(MuseumJson museum)` - обновление
- `MuseumJson create(MuseumJson museum)` - создание

**Особенности:**

- Автоматическое связывание с `CountryEntity` по ID или имени
- Конвертация base64 изображений через `StringAsBytes`
- `@Transactional` для write операций

### UserService

**Методы:**

- `UserJson getCurrentUser(String username)` - получение текущего пользователя
- `UserJson update(UserJson user)` - обновление профиля
- `UserJson createNewUserIfNotPresent(String username)` - создание при первом входе

**Особенности:**

- Автоматическое создание пользователя при первой аутентификации
- Обработка аватаров через `BytesAsString` / `StringAsBytes`

## Обработка ошибок

### GlobalExceptionHandler

**Обрабатываемые исключения:**

1. **MethodArgumentNotValidException**
    - Ошибки валидации `@Valid`
    - Возвращает список полей с ошибками + `400 BAD_REQUEST`

2. **ConstraintViolationException** `400 BAD_REQUEST`
    - Нарушение constraint'ов БД
    - Возвращает список сообщений

3. **ResourceNotFoundException** `404 NOT_FOUND`
    - Ресурс не найден
    - Кастомное сообщение

4. **MethodArgumentTypeMismatchException** `400 BAD_REQUEST`
    - Неверный формат параметра (например, невалидный UUID)

5. **IllegalArgumentException** `400 BAD_REQUEST`
    - Некорректные аргументы

6. **IllegalStateException** `409 CONFLICT`
    - Операция невозможна в текущем состоянии

7. **Exception** `500 INTERNAL_SERVER_ERROR`
    - Все остальные ошибки
    - Детали не возвращаются клиенту

### ErrorJson структура

```json
{
  "errors": [
    "Музей не найден по id: 123e4567-e89b-12d3-a456-426614174000"
  ]
}
```

Или для валидации:

```json
{
  "errors": [
    "title: не должно быть пустым",
    "city: не должно быть пустым"
  ]
}
```

## JSON сериализация

Все DTO реализованы в виде Record, аннотированы `@JsonInclude(NON_NULL)` для исключения null полей из JSON response:

```java

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserJson(UUID id, String username, String firstname,
                       String lastname, String avatar) {
  // ...
}
```

**Результат:**

```json
{
  "id": "uuid",
  "username": "user@example.com",
  "firstname": "Иван"
  // lastname и avatar не включены, если null
}
```

## База данных

### Flyway миграции

Расположение: `src/main/resources/db/migration/rococo-api/`

## Запуск

### Требования

- Java 21
- docker
- Gradle 9.2.1

### Локальный запуск

```bash
# 1. Запустить MySQL
bash localenv.sh
# 3. Запустить Resource Server (rcc-api)
cd ../rcc-api
../gradlew bootRun
# Или через IDE
# Main class: io.student.rococo.RococoApiApplication
```

### Проверка работоспособности

```bash
# Публичный endpoint
curl http://localhost:8080/api/session

# Получение списка стран
curl http://localhost:8080/api/country

# С аутентификацией
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/user
```
