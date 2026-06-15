# 04 — DTO запросов (Register, Login)

## Цель

Входные DTO для регистрации и входа с `@JsonProperty` в snake_case (как в spec).


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [01-user-role.md](./01-user-role.md)

## Зависимости

Validation starter — в [06](./06-password-validation.md). Здесь только структура полей.

## Шаги (Red → Green → Refactor)

### 1. `RegisterDto`

**Файл:** `src/main/java/ru/videoplatform/auth/dto/RegisterDto.java`

```json
{
  "login": "student01",
  "password": "Str0ng!!!Pass"
}
```

```java
public class RegisterDto {
    @JsonProperty("login")
    private String login;

    @JsonProperty("password")
    private String password;
}
```

Публичный `POST /api/v1/auth/register` — только STUDENT ([09](./09-service-register.md)).

### 2. `TeacherRegisterDto`

**Файл:** `dto/TeacherRegisterDto.java`

Те же поля. Используется в `POST /api/v1/auth/register/teacher` (только ADMIN).

### 3. `LoginDto`

**Файл:** `dto/LoginDto.java`

```json
{
  "login": "teacher123",
  "password": "Str0ng!!!Pass"
}
```

### 4. `RefreshDto`

**Файл:** `dto/RefreshDto.java`

```json
{
  "refresh_token": "rfr_987654321_xyz"
}
```

## Критерии готовности

- [ ] DTO в пакете `dto`
- [ ] JSON-поля совпадают с [spec.md](../spec.md)
- [ ] Компиляция без ошибок

## Связанные задачи

- [06-password-validation.md](./06-password-validation.md)
- [12-controller-rest.md](./12-controller-rest.md)
