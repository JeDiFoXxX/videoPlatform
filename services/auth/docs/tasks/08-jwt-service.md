# 08 — JwtService

## Цель

Генерация и валидация JWT access-токенов (JJWT): claims `sub`, `login`, `role`, `jti`, `exp`.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | `JwtServiceTest` — unit-тесты без Spring |
| **Green** | `JwtService` + `JwtProperties` |
| **Refactor** | Вынести TTL и secret в `application.properties` |

## Предусловия

- [01-user-role.md](./01-user-role.md)
- [02-entity-user.md](./02-entity-user.md)

## Зависимости (`pom.xml`)

### JJWT 0.13.x

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
```

### Конфигурация

**Файл:** `config/JwtProperties.java`

```java
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String secret;
    private Duration accessTokenLifetime;   // 900s
    private Duration refreshTokenLifetime;  // 30d
}
```

**Файл:** `application.properties`

```properties
app.security.jwt.secret=${JWT_SECRET:my-super-secret-key-32-symbols-minimum-length}
app.security.jwt.access-token-lifetime=900s
app.security.jwt.refresh-token-lifetime=30d
```

## Шаги (Red → Green → Refactor)

### 1. `JwtService`

**Файл:** `src/main/java/ru/videoplatform/auth/service/JwtService.java`

| Метод | Назначение |
|-------|------------|
| `generateAccessToken(User user)` | JWT через `Jwts.builder()`, TTL из `JwtProperties` |
| `extractJti(String token)` | claim `jti` (`.id(...)`) для blacklist |
| `isTokenValid(String token)` | подпись + exp (blacklist — в [11](./11-security-config.md)) |

Claims:

```json
{
  "sub": "uuid",
  "login": "student01",
  "role": "STUDENT",
  "jti": "unique-id"
}
```

> Проверка blacklist в `isTokenValid` — **не в этой задаче**; добавить в [11](./11-security-config.md) (`JwtAuthenticationFilter`).

### 2. Unit-тесты `JwtServiceTest`

| Кейс | Ожидание |
|------|----------|
| `extractJti` на валидном токене | not null |
| `extractJti` на повреждённом токене | null |
| `isTokenValid` на свежем токене | true |
| `isTokenValid` при TTL = 0 | false |

Тесты **без** `@SpringBootTest` — `new JwtService(jwtProperties)`.

## Критерии готовности

- [x] TTL access = 900 сек (`app.security.jwt.access-token-lifetime`)
- [x] Claims `sub`, `login`, `role`, `jti`
- [x] `role` читается из JWT без запроса к БД ([spec](../spec.md))
- [x] `JwtServiceTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=JwtServiceTest -pl services/auth
```

## Связанные задачи

- [10-service-session.md](./10-service-session.md)
- [11-security-config.md](./11-security-config.md)
