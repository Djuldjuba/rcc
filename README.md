# Rococo

Микросервисное веб-приложение «галерея искусства» — художники, картины, музеи, страны и профили пользователей.

Frontend — **SvelteKit**. Backend — **Spring Boot 4** + **gRPC** + **OAuth 2.0**.  
Единая точка входа для фронта — API Gateway. Между сервисами — gRPC. Асинхронные события (регистрация / обновление пользователя) — Kafka.

---

## Архитектура

```
┌──────────────┐
│  rcc-client  │  SvelteKit (SPA)
│   :3000      │
└──────┬───────┘
       │ REST
       ▼
┌──────────────┐
│ rcc-gateway  │  API Gateway
│   :8085      │
└──────┬───────┘
       │ gRPC
       ├──────────────────┬──────────────────┬──────────────────┬──────────────────┐
       ▼                  ▼                  ▼                  ▼                  ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│ rcc-artist  │   │rcc-painting │   │ rcc-museum  │   │   rcc-geo   │   │rcc-userdata │
│ REST :8086  │   │ REST :8087  │   │ REST :8088  │   │ REST :8091  │   │ REST :8090  │
│ gRPC :9090  │   │ gRPC :9091  │   │ gRPC :9093  │   │ gRPC :9094  │   │ gRPC :9095  │
└─────────────┘   └─────────────┘   └──────┬──────┘   └─────────────┘   └──────┬──────┘
                                           │ gRPC                              │
                                           └───────────────────────────────────┘
                                                         ▲
┌──────────────┐                                         │
│   rcc-auth   │  OAuth2 / OIDC Authorization Server     │
│    :9000     │─────────────────────────────────────────┘
└──────┬───────┘
       │ Kafka (users, user-updates)
       ▼
┌────────────────────────┐
│ rcc-log-kafka-service  │  Consumer логов
│        :8092           │
└────────────────────────┘
```
Подробности по каждому сервису — в соответствующих `README.md` внутри модулей.

---

## Минимальные предусловия для работы с проектом

#### 0. Если у вас ОС Windows

Во-первых, и в-главных, необходимо использовать [bash terminal](https://www.geeksforgeeks.org/working-on-git-bash/), а
не powershell.
[Полезное и короткое видео о git bash](https://www.youtube.com/watch?v=zM9Mb-otqww)
Обязательно добавьте bash терминал в [качестве терминала в вашей IDE (IDEA, PyCharm)](https://stackoverflow.com/questions/20573213/embed-git-bash-in-pycharm-as-external-tool-and-work-with-it-in-pycharm-window-w)
Во-вторых, если у вас что-то не работает - пишите, будем вместе дополнять README, т.к. изначально
проект разработан под *nix.

#### 1. Установить Docker (если не установлен)

Мы будем использовать Docker для БД (MySQL), Kafka, а также для запуска всего стека через docker-compose.

[Установка на Windows](https://docs.docker.com/desktop/install/windows-install/)

[Установка на Mac](https://docs.docker.com/desktop/install/mac-install/) (Для ARM и Intel разные пакеты)

[Установка на Linux](https://docs.docker.com/desktop/install/linux-install/)

После установки и запуска docker daemon необходимо убедиться в работе команд docker, например `docker -v`:

```bash
docker -v
Docker version 27.x.x, build ...
```

#### 2. Спуллить контейнеры mysql:8.4.7, zookeeper и kafka версии 7.7.7

```bash
docker pull mysql:8.4.7
docker pull confluentinc/cp-zookeeper:7.7.7
docker pull confluentinc/cp-kafka:7.7.7
```

После `pull` вы увидите спуленные image командой `docker images`:

```bash
docker images
REPOSITORY                    TAG       IMAGE ID       CREATED        SIZE
mysql                         8.4.7     ...            ...            ...
confluentinc/cp-kafka         7.7.7     ...            ...            ...
confluentinc/cp-zookeeper     7.7.7     ...            ...            ...
```

#### 3. Создать volume для сохранения данных из БД в docker на вашем компьютере

```bash
docker volume create rcc-volume
```

#### 4. Запустить БД, zookeeper и kafka

Запустив скрипт (для Windows необходимо использовать bash terminal: gitbash, cygwin или wsl):

```bash
bash localenv.sh
```

Или выполнив последовательно команды:

```bash
docker run --name rcc-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=secret \
  -v rcc-volume:/var/lib/mysql \
  -d mysql:8.4.7

docker run --name=zookeeper \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -p 2181:2181 \
  -d confluentinc/cp-zookeeper:7.7.7

docker run --name=kafka \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=$(docker inspect zookeeper --format='{{ .NetworkSettings.Networks.bridge.IPAddress }}'):2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -p 9092:9092 \
  -d confluentinc/cp-kafka:7.7.7
```

[Про IP zookeeper](https://github.com/confluentinc/cp-docker-images/issues/801#issuecomment-692085103)

#### 5. Установить Java версии 21

Это необходимо, т.к. проект использует синтаксис Java 21.

Версию установленной Java необходимо проверить командой `java -version`:

```bash
java -version
openjdk version "21.0.1" 2023-10-17 LTS
OpenJDK Runtime Environment Temurin-21.0.1+12 (build 21.0.1+12-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.1+12 (build 21.0.1+12-LTS, mixed mode)
```

Если у вас несколько версий Java одновременно — то хотя бы одна из них должна быть 21.  
Если java не установлена вовсе, то рекомендуется установить OpenJDK (например, с https://adoptium.net/).

#### 6. Установить пакетный менеджер для сборки front-end npm

[Инструкция](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm).  
Рекомендованная версия — Node.js - 22.6.0, npm - 10.8.2

```bash
node -v && npm -v
v20.18.0
10.8.2
```

---

## Локальный запуск

### 1. Инфраструктура (MySQL + Kafka)

```bash
bash localenv.sh
```

### 2. Backend-сервисы

Рекомендуемый порядок:

```bash
# Authorization Server
./gradlew :rcc-auth:bootRun

# Domain services (можно параллельно)
./gradlew :rcc-artist:bootRun
./gradlew :rcc-painting:bootRun
./gradlew :rcc-museum:bootRun
./gradlew :rcc-geo:bootRun
./gradlew :rcc-userdata:bootRun
./gradlew :rcc-log-kafka-service:bootRun

# Gateway (после всех downstream)
./gradlew :rcc-gateway:bootRun
```

Либо запускать main-классы из IDE.

### 3. Frontend

```bash
cd rcc-client
npm ci
npm run dev
```

Фронт будет доступен: [http://localhost:3000](http://localhost:3000)

Конфигурация (`.env`):

```dotenv
VITE_AUTH_URL=http://localhost:9000
VITE_API_URL=http://localhost:8085
VITE_FRONT_HOST=localhost
VITE_FRONT_URL=http://localhost:3000
VITE_CLIENT_ID=client
```

### 4. Проверка

| Сервис        | URL                                                                 |
|---------------|---------------------------------------------------------------------|
| Frontend      | http://localhost:3000                                               |
| Gateway       | http://localhost:8085                                               |
| Auth          | http://localhost:9000                                               |
| OpenID Config | http://localhost:9000/.well-known/openid-configuration              |

GET-запросы к каталогу (artists / museums / paintings / countries) публичные.  
Создание / изменение / удаление и работа с профилем требуют JWT.

---

## Запуск в Docker

### Полный стек (dev)

```bash
bash docker-compose-dev.sh
```

Скрипт:

1. Останавливает старые контейнеры
2. Собирает backend-образы через Jib
3. Собирает frontend (`rcc-client`)
4. Поднимает весь стек через `docker-compose.yml`

После запуска:

| Сервис              | URL                          |
|---------------------|------------------------------|
| Frontend            | http://localhost:3000        |
| Gateway             | http://localhost:8085        |
| Auth                | http://localhost:9000        |
| Kafka UI            | http://localhost:8080        |

---

## Тесты

### Unit / Integration (без e2e)

```bash
./gradlew test -x :rcc-e2e:test
```

### E2E (Docker + Selenoid + Allure)

Полная пересборка и запуск:

```bash
bash docker-compose-e2e.sh
# или с указанием браузера
bash docker-compose-e2e.sh chrome
```

Быстрый запуск без пересборки образов:

```bash
bash docker-compose-e2e-quick.sh
```

После старта:

| Сервис           | URL                                                                 |
|------------------|---------------------------------------------------------------------|
| Selenoid UI      | http://localhost:9099/                                              |
| Allure UI        | http://localhost:5252/                                              |
| Allure report    | http://localhost:5050/allure-docker-service/projects/rcc/reports/latest/index.html |

Логи e2e:

```bash
docker logs -f rcc-e2e
```

---

## CI / CD

GitHub Actions (`.github/workflows/e2e.yml`):

- Unit & Integration тесты
- Сборка backend (Jib) + frontend + e2e-образа
- Запуск e2e в Docker Compose (`profile: test`)
- Генерация Allure-отчёта и публикация на GitHub Pages
- Комментарий в PR со ссылкой на отчёт

---

## Структура репозитория

```
rcc/
├── rcc-auth/                 # OAuth2 Authorization Server
├── rcc-gateway/              # API Gateway
├── rcc-artist/               # Сервис художников
├── rcc-painting/             # Сервис картин
├── rcc-museum/               # Сервис музеев
├── rcc-geo/                  # Справочник стран
├── rcc-userdata/             # Профили пользователей
├── rcc-log-kafka-service/    # Kafka consumer логов
├── rcc-client/               # Frontend (SvelteKit)
├── rcc-e2e/                  # E2E / UI / gRPC / Kafka тесты
├── docker-compose.yml
├── docker-compose-dev.sh
├── docker-compose-e2e.sh
├── docker-compose-e2e-quick.sh
├── localenv.sh
├── settings.gradle
├── build.gradle
└── README.md
```

---

## Технологический стек

**Backend**

- Java 21
- Spring Boot 4.x
- Spring Security (OAuth 2.0 Resource Server / Authorization Server)
- Spring Data JPA + Flyway
- MySQL 8
- gRPC + Protobuf
- Spring Kafka
- Lombok
- Google Jib (Docker-образы)

**Тесты**

- JUnit 5
- Selenide + Selenoid
- Allure
- gRPC-тесты
- Kafka consumers
- AShot (screenshot comparison)
