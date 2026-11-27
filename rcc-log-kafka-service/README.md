# RCC-Log-Kafka-Service

## Описание

`rcc-log-kafka-service` — микросервис проекта Rococo, отвечающий за сбор и хранение логов событий.  
Слушает Kafka-топики `users` и `user-updates`, сохраняет входящие события в базу MySQL.

Сервис не предоставляет REST/gRPC API — это чистый consumer, который пишет логи действий пользователей.

## Архитектура

### Технологический стек

- **Spring Boot 3.x / 4.x**
- **Spring Data JPA**
- **Flyway** — миграции БД
- **MySQL 8.0**
- **Spring Kafka**
- **Lombok**

### Структура модуля

```
rcc-log-kafka-service/
├── src/main/java/io/student/rcc/logkafka/
│   ├── RccLogKafkaApplication.java          # Точка входа
│   ├── data/
│   │   ├── LogEntity.java                   # JPA Entity
│   │   └── LogRepository.java               # Spring Data JPA
│   ├── kafka/
│   │   ├── LogEvent.java                    # DTO события
│   │   └── LogEventsListener.java           # Kafka listeners
│   └── service/
│       └── LogService.java                  # Сохранение логов
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   └── db/migration/
│       └── V1__init_log_table.sql
└── build.gradle
```

## Kafka

### Входящие события (Consumer)

**Группа:** `rcc-log-kafka-service`

| Топик          | Метод слушателя     | Описание                          |
|----------------|---------------------|-----------------------------------|
| `users`        | `onUserRegistered`  | Регистрация пользователя          |
| `user-updates` | `onUserUpdated`     | Обновление профиля пользователя   |

**Событие:** `LogEvent`

```java
record LogEvent(
    String userId,
    String username,
    String eventType,
    LocalDateTime eventDate
)
```

`LogEventsListener`:

- Валидирует входящее событие (не `null`, обязательные поля заполнены)
- Логирует получение
- Вызывает `LogService.save(...)` с `photoId = null`

## Сервисный слой

### LogService

| Метод | Описание |
|-------|----------|
| `save(userId, photoId, eventType, eventDate)` | Создаёт `LogEntity` и сохраняет в БД |

**Особенности:**

- `@Transactional` на методе сохранения
- `logDate` всегда устанавливается в `LocalDateTime.now()`
- `photoId` в текущей реализации всегда `null` (поле зарезервировано)

### LogEntity

| Поле        | Тип            | Описание                    |
|-------------|----------------|-----------------------------|
| `id`        | `UUID`         | Primary key (auto-generated)|
| `userId`    | `String`       | ID пользователя             |
| `photoId`   | `String`       | ID фото (опционально)       |
| `eventType` | `String`       | Тип события                 |
| `eventDate` | `LocalDateTime`| Дата события из Kafka       |
| `logDate`   | `LocalDateTime`| Дата записи в лог           |

## База данных

### Flyway миграция

`V1__init_log_table.sql`:

```sql
CREATE TABLE IF NOT EXISTS `log`
(
    id         BINARY(16)  NOT NULL,
    user_id    VARCHAR(36),
    photo_id   VARCHAR(36),
    event_type VARCHAR(50) NOT NULL,
    event_date DATETIME    NOT NULL,
    log_date   DATETIME    NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_log_event_type ON `log`(event_type);
CREATE INDEX idx_log_user_id ON `log`(user_id);
CREATE INDEX idx_log_event_date ON `log`(event_date);
```

### Репозиторий

```java
public interface LogRepository extends JpaRepository<LogEntity, UUID> {
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
./gradlew :rcc-log-kafka-service:bootRun
```

Или через IDE — main class: `io.student.rcc.logkafka.RccLogKafkaApplication`

### Порты

| Протокол | Порт |
|----------|------|
| HTTP     | 8092 |

(REST API отсутствует, порт используется только для Actuator / health при необходимости)

### Профили

- `application.yml` — локальная разработка
- `application-docker.yml` — Docker / docker-compose

### Проверка

Сервис не имеет публичных эндпойнтов.  
Проверить работу можно по логам при появлении сообщений в топиках `users` и `user-updates`, а также по записям в таблице `log`.
