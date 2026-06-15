# 03 — RefreshToken и BlacklistedToken

## Цель

Сущности для долгоживущих refresh-токенов и чёрного списка отозванных access-токенов.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [02-entity-user.md](./02-entity-user.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. Entity `RefreshToken`

**Файл:** `src/main/java/ru/videoplatform/auth/model/RefreshToken.java`

| Поле | Тип | Назначение |
|------|-----|------------|
| `id` | `UUID` | PK |
| `token` | `String` | unique, opaque id (не JWT) |
| `user` | `User` | `@ManyToOne` |
| `expiresAt` | `Instant` | срок действия |
| `revoked` | `boolean` | отозван при logout |

### 2. Entity `BlacklistedToken`

**Файл:** `src/main/java/ru/videoplatform/auth/model/BlacklistedToken.java`

| Поле | Тип | Назначение |
|------|-----|------------|
| `id` | `UUID` | PK |
| `jti` | `String` | unique, claim JWT |
| `expiresAt` | `Instant` | TTL = exp access token |

### 3. Flyway (в [07](./07-repositories-flyway.md))

```sql
CREATE TABLE refresh_tokens (...);
CREATE TABLE blacklisted_tokens (...);
```

### 4. Связь со spec

- **Refresh:** проверка в БД при `POST /api/v1/auth/refresh`
- **Logout:** удаление refresh + запись `jti` access в blacklist

## Критерии готовности

- [ ] Обе сущности в `model`
- [ ] FK `refresh_tokens.user_id → users.id`
- [ ] Уникальные индексы на `token` и `jti`

## Связанные задачи

- [07-repositories-flyway.md](./07-repositories-flyway.md)
- [10-service-session.md](./10-service-session.md)
