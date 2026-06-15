# 10 — AuthService: login, refresh, logout

## Цель

Аутентификация, обновление access-токена, отзыв сессии.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [08-jwt-service.md](./08-jwt-service.md)
- [09-service-register.md](./09-service-register.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. `login(LoginDto dto)` → `AuthResponseDto`

| Шаг | Действие |
|-----|----------|
| 1 | `findByLogin` → 401 если нет |
| 2 | `passwordEncoder.matches` → 401 если неверно |
| 3 | `jwtService.generateAccessToken(user)` |
| 4 | Создать opaque `RefreshToken`, сохранить в БД |
| 5 | Вернуть `AuthResponseDto` (`expires_in: 900`) |

### 2. `refresh(RefreshDto dto)` → `AuthResponseDto`

| Шаг | Действие |
|-----|----------|
| 1 | Найти refresh по token, `revoked = false`, не expired |
| 2 | Новый access JWT |
| 3 | (опционально) rotate refresh token |
| 4 | Вернуть `AuthResponseDto` |

### 3. `logout(String accessToken, String refreshToken)`

| Шаг | Действие |
|-----|----------|
| 1 | `refreshToken.revoked = true` |
| 2 | `jti` access → `BlacklistedToken` |
| 3 | 204 No Content |

### 4. Unit-тесты `AuthServiceSessionTest`

- Успешный login → tokens
- Неверный пароль → 401
- Refresh с валидным token → новый access
- Logout → refresh revoked, jti в blacklist

## Критерии готовности

- [ ] Login/refresh/logout по [spec.md](../spec.md)
- [ ] `expires_in` = 900
- [ ] Тесты зелёные

## Связанные задачи

- [12-controller-rest.md](./12-controller-rest.md)
- [11-security-config.md](./11-security-config.md)
