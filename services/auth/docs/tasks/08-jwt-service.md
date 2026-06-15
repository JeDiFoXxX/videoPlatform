# 08 — JwtService

## Цель

Генерация и валидация JWT access-токенов: claims `sub` (user id), `login`, `role`, `jti`, `exp`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [01-user-role.md](./01-user-role.md)
- [02-entity-user.md](./02-entity-user.md)

## Зависимости (`pom.xml`)

### Добавить (JJWT 0.12.x)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### Конфигурация

```properties
auth.jwt.secret=${JWT_SECRET:change-me-in-production}
auth.jwt.access-ttl-seconds=900
```

## Шаги (Red → Green → Refactor)

### 1. `JwtService`

**Файл:** `src/main/java/ru/videoplatform/auth/service/JwtService.java`

| Метод | Назначение |
|-------|------------|
| `generateAccessToken(User user)` | JWT, TTL 900 сек |
| `parseToken(String token)` | `Claims` или custom `JwtClaims` |
| `extractJti(String token)` | для blacklist при logout |
| `isTokenValid(String token)` | подпись + exp + не в blacklist |

Claims:

```json
{
  "sub": "uuid",
  "login": "student01",
  "role": "STUDENT",
  "jti": "unique-id"
}
```

### 2. Unit-тесты `JwtServiceTest`

| Кейс | Ожидание |
|------|----------|
| generate + parse | `sub`, `role` совпадают |
| expired token | `isTokenValid` → false |
| wrong signature | exception |

## Критерии готовности

- [ ] TTL access = 900 сек
- [ ] `role` читается без запроса к БД ([spec](../spec.md) п.5)
- [ ] `JwtServiceTest` зелёный

## Связанные задачи

- [10-service-session.md](./10-service-session.md)
- [11-security-config.md](./11-security-config.md)
