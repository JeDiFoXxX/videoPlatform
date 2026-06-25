# 10 — AuthService: login, refresh, logout

## Цель

Аутентификация, обновление access-токена, отзыв сессии и запись access JWT в blacklist.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Кейсы сессии в `AuthServiceTest` — падают без реализации |
| **Green** | `login`, `refresh`, `logout` в `AuthService` |
| **Refactor** | Сборка `AuthResponseDto` через builder |

## Предусловия

- [08-jwt-service.md](./08-jwt-service.md) — `JwtService` (JJWT)
- [09-service-register.md](./09-service-register.md) — `AuthService`, `AuthException`

## Зависимости

Без новых Maven-зависимостей. Используются:

- `JwtService`, `JwtProperties`
- `RefreshTokenRepository`, `BlacklistedTokenRepository`
- `PasswordEncoder`

## Repository-тесты (опционально)

| Файл | Кейсы |
|------|-------|
| `RefreshTokenRepositoryTest` | `findByTokenAndRevokedFalse` |
| `BlacklistedTokenRepositoryTest` | `existsByJti` |

`@DataJpaTest` + `@ActiveProfiles("test")`. Можно добавить параллельно или в [14](./14-integration-tests.md).

## Шаги (Red → Green → Refactor)

### Red — тесты в `AuthServiceTest`

| Кейс | Ожидание |
|------|----------|
| login — успех | `AuthResponseDto` с access + refresh, `expiresIn = 900` |
| login — пользователь не найден | 401 |
| login — неверный пароль | 401 |
| refresh — валидный token | новый access, тот же refresh |
| refresh — сессия не найдена / revoked | 401 |
| refresh — сессия истекла | 401 |
| logout | refresh `revoked = true`, `BlacklistedToken` сохранён |

### Green — методы `AuthService`

#### `login(LoginDto dto)` → `AuthResponseDto`

| Шаг | Действие |
|-----|----------|
| 1 | `findByLogin` → 401 если нет |
| 2 | `passwordEncoder.matches` → 401 если неверно |
| 3 | `jwtService.generateAccessToken(user)` |
| 4 | Создать opaque `RefreshToken` (`revoked = false` по умолчанию), сохранить |
| 5 | Вернуть `AuthResponseDto` с `expiresIn` из `JwtProperties` |

#### `refresh(RefreshDto dto)` → `AuthResponseDto`

| Шаг | Действие |
|-----|----------|
| 1 | `findByTokenAndRevokedFalse`, проверить `expiresAt` |
| 2 | Новый access JWT |
| 3 | Вернуть `AuthResponseDto` (refresh без rotation) |

#### `logout(String accessToken, String refreshToken)`

| Шаг | Действие |
|-----|----------|
| 1 | Найти refresh, установить `revoked = true` |
| 2 | `jwtService.extractJti(accessToken)` → `BlacklistedToken` |
| 3 | void (204 в контроллере) |

### Blacklist при валидации access (отложено)

Отклонение blacklisted access-токена — в [11](./11-security-config.md):

```java
// JwtAuthenticationFilter
jwtService.isTokenValid(token)
    && !blacklistedTokenRepository.existsByJti(jwtService.extractJti(token))
```

## Критерии готовности

- [x] Login / refresh / logout по [spec.md](../spec.md)
- [x] `expires_in` = 900
- [x] Logout пишет `jti` в `blacklisted_tokens`
- [x] Кейсы сессии в `AuthServiceTest` зелёные
- [x] Blacklist проверяется в JWT filter ([11](./11-security-config.md))

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=AuthServiceTest -pl services/auth
```

## Связанные задачи

- [11-security-config.md](./11-security-config.md)
- [12-controller-rest.md](./12-controller-rest.md)
- [14-integration-tests.md](./14-integration-tests.md)
