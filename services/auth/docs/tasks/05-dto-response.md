# 05 — DTO ответов (AuthResponse)

## Цель

Единый формат ответа с токенами после login/refresh.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [04-dto-request.md](./04-dto-request.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. `AuthResponseDto`

**Файл:** `src/main/java/ru/videoplatform/auth/dto/AuthResponseDto.java`

```json
{
  "type": "auth_success",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "rfr_987654321_xyz",
  "expires_in": 900
}
```

| Поле | Тип | Примечание |
|------|-----|------------|
| `type` | `String` | константа `"auth_success"` |
| `access_token` | `String` | JWT |
| `refresh_token` | `String` | opaque token |
| `expires_in` | `int` | 900 сек (15 мин) |

```java
public class AuthResponseDto {
    @JsonProperty("type")
    private String type = "auth_success";

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;
}
```

### 2. Factory в сервисе

`AuthService` ([10](./10-service-session.md)) заполняет DTO после успешного login/refresh.

### 3. Unit-тест сериализации

**Файл:** `src/test/java/ru/videoplatform/auth/dto/AuthResponseDtoTest.java`

- `ObjectMapper` → JSON содержит `access_token`, `expires_in: 900`

## Критерии готовности

- [ ] DTO в пакете `dto`
- [ ] `expires_in` = 900 по spec
- [ ] Тест сериализации зелёный

## Связанные задачи

- [08-jwt-service.md](./08-jwt-service.md)
- [10-service-session.md](./10-service-session.md)
