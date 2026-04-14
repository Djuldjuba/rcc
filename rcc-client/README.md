# rcc-client

Frontend приложения Rococo.

## Стек

- SvelteKit + Vite
- TypeScript
- TailwindCSS
- Skeleton UI (`@skeletonlabs/skeleton`)

Сборка настроена как **SPA**:

- `src/routes/+layout.ts`: `export const ssr = false` (рендеринг только на клиенте)
- `svelte.config.js`: `@sveltejs/adapter-static` + `fallback: 'index.html'`

## Архитектура

### Роутинг (страницы)

SvelteKit routes лежат в `src/routes`.

- `/` - `src/routes/+page.svelte` (главное меню)
- `/painting`, `/artist`, `/museum` - разделы каталога
- `/authorized` - обработка OAuth2 callback (получение токена по `code`)
- `/logout` - очистка сессии на клиенте и редирект на `/`

Глобальный layout: `src/routes/+layout.svelte`:

- инициализация Skeleton stores
- загрузка сессии (через `apiClient.loadSession()`)
- dev-проверка хоста и редирект на `VITE_FRONT_HOST` (чтобы URL соответствовал ожидаемому)

### UI слой

- `src/lib/components` - переиспользуемые компоненты (списки, меню, лоадер, модалки и т.д.)
- `src/lib/components/forms` - формы создания/редактирования сущностей
- `src/lib/components/content` - контейнеры/страничные компоненты (например, `CommonPage`, `AuthorizedContent`)

### Хранилища состояния

`src/lib/stores` - Svelte stores для списков/детальных сущностей и сессии.

Пример: `sessionStore` хранит `{ user, isLoading }`.

### API слой

`src/lib/api`:

- `apiClient.ts` - вызовы backend API (`VITE_API_URL`), общий `commonFetch` с:
  - `credentials: 'include'`
  - `Authorization: Bearer <id_token>` (если токен сохранён)
  - обработкой `401` через `clearSession()`
- `authClient.ts` - запрос токена в auth service (`VITE_AUTH_URL`)
- `authUtils.ts` - вспомогательная логика OAuth2/OIDC (PKCE):
  - генерация `code_verifier`/`code_challenge`
  - редирект на `/oauth2/authorize`
  - формирование параметров для `/oauth2/token`

Токен хранится в `localStorage` (`id_token`).

### Типы и утилиты

- `src/lib/types` - TS-типы доменных сущностей
- `src/lib/helpers` - утилиты (пагинация/query builder, валидация, подготовка модалок и т.д.)

## Переменные окружения

Используются Vite env-переменные (см. `.env`):

- `VITE_API_URL` - base URL backend (далее используется `${VITE_API_URL}/api`)
- `VITE_AUTH_URL` - base URL auth service
- `VITE_CLIENT_ID` - client id для OAuth2/OIDC
- `VITE_FRONT_URL` - публичный URL фронта (используется в `redirect_uri`)
- `VITE_FRONT_HOST` - хост, на который фронт будет себя "нормализовать" при локальной разработке

Пример `.env` (локально):

```dotenv
VITE_AUTH_URL=http://localhost:9000
VITE_API_URL=http://localhost:8080
VITE_FRONT_HOST=localhost
VITE_FRONT_URL=http://localhost:3000
VITE_CLIENT_ID=client
```

## Запуск

### Требования

- Node.js (рекомендуется LTS)
- npm

### Установка

```bash
npm ci
```

### Dev режим

```bash
npm run dev
```

По умолчанию dev-сервер стартует на `http://<VITE_FRONT_HOST>:3000`.

### Build

```bash
npm run build
npm run preview
```

### Сборки с режимами (modes)

В проекте есть дополнительные скрипты:

- `npm run build:docker` - `vite build --mode docker`
- `npm run build:stage` - `vite build --mode stage`

Vite подхватывает переменные окружения через `loadEnv(mode, process.cwd())` (см. `vite.config.ts`).

Если для режимов `docker/stage` нужны отдельные значения, добавь файлы вида:

- `.env.docker`
- `.env.stage`

(с теми же ключами `VITE_*`).

## Troubleshooting

### "Перекидывает" с 127.0.0.1 на localhost в dev

Это ожидаемо: в `src/routes/+layout.svelte` есть проверка текущего `window.location.host` и редирект на `VITE_FRONT_HOST`.

### 401 / разлогинивает

При `401` в `apiClient.commonFetch` вызывается `clearSession()`, токен удаляется из `localStorage`.

---

