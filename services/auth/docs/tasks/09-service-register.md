# 09 — AuthService: регистрация

## Цель

Регистрация ученика (публично) и учителя (только ADMIN).

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Кейсы регистрации в `AuthServiceTest` — падают без сервиса |
| **Green** | `AuthService.registerStudent`, `registerTeacher` |
| **Refactor** | Общий `createUser` + `validateLoginUniqueness` |

## Предусловия

- [06-password-validation.md](./06-password-validation.md) — `@ValidPassword`, `@NotBlank` на `RegisterDto` и `TeacherRegisterDto`
- [07-repositories-liquibase.md](./07-repositories-liquibase.md) — `UserRepository`

## Зависимости

`PasswordEncoder` — BCrypt bean в `SecurityConfig` ([11](./11-security-config.md), bean уже есть).

## Шаги (Red → Green → Refactor)

### Red — тесты в `AuthServiceTest`

**Файл:** `src/test/java/ru/videoplatform/auth/service/AuthServiceTest.java`

| Кейс | Ожидание |
|------|----------|
| `registerStudent` — успех | `User` с `role = STUDENT`, пароль захэширован |
| `registerStudent` — дубликат login | `AuthException` / 409 |
| `registerTeacher` — успех | `User` с `role = TEACHER` |
| `registerTeacher` — дубликат login | `AuthException` / 409 |

Mock: `UserRepository`, `PasswordEncoder`.

### Green — `AuthService`

**Файл:** `src/main/java/ru/videoplatform/auth/service/AuthService.java`

#### `registerStudent(RegisterDto dto)` → `User`

| Шаг | Действие |
|-----|----------|
| 1 | `existsByLogin` → 409 Conflict |
| 2 | `passwordEncoder.encode(dto.getPassword())` |
| 3 | Сохранить `User` с `role = STUDENT` |

#### `registerTeacher(TeacherRegisterDto dto)` → `User`

- Те же правила валидации, что у `RegisterDto`
- Роль `TEACHER`
- Вызывается только из защищённого эндпоинта ([12](./12-controller-rest.md))

### Исключения

**Файл:** `exception/AuthException.java` — extends `ResponseStatusException`

| Factory | HTTP |
|---------|------|
| `conflict(message)` | 409 |
| `unauthorized(message)` | 401 |
| `badRequest(message)` | 400 |

| Ситуация | HTTP |
|----------|------|
| Логин занят | 409 |
| Невалидный пароль / login | 400 (Bean Validation в [12](./12-controller-rest.md)) |

> Единый JSON для ошибок — [13-exception-handler.md](./13-exception-handler.md).

## Критерии готовности

- [x] STUDENT — публичная регистрация
- [x] TEACHER — метод в сервисе (защита эндпоинта — [11](./11-security-config.md))
- [x] Пароль хранится только как BCrypt-хэш
- [x] Кейсы регистрации в `AuthServiceTest` зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=AuthServiceTest -pl services/auth
```

## Связанные задачи

- [10-service-session.md](./10-service-session.md) — login/refresh/logout в том же `AuthService`
- [11-security-config.md](./11-security-config.md)
- [12-controller-rest.md](./12-controller-rest.md)
