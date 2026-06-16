# 09 — AuthService: регистрация

## Цель

Регистрация ученика (публично) и учителя (только ADMIN).


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [06-password-validation.md](./06-password-validation.md)
- [07-repositories-liquibase.md](./07-repositories-liquibase.md)

## Зависимости

`PasswordEncoder` — BCrypt из Spring Security (bean в [11](./11-security-config.md)).

## Шаги (Red → Green → Refactor)

### 1. `AuthService.registerStudent(RegisterDto dto)`

| Шаг | Действие |
|-----|----------|
| 1 | Проверить `existsByLogin` → 409 Conflict |
| 2 | `passwordEncoder.encode(dto.getPassword())` |
| 3 | Сохранить `User` с `role = STUDENT` |
| 4 | Вернуть `User` или void (без токенов — только регистрация) |

### 2. `AuthService.registerTeacher(TeacherRegisterDto dto)`

- Роль `TEACHER`
- Вызывается только из защищённого эндпоинта ([12](./12-controller-rest.md))

### 3. Исключения

| Ситуация | HTTP |
|----------|------|
| Логин занят | 409 |
| Невалидный пароль | 400 (validation) |

**Файл:** `exception/AuthException.java` — factories `conflict`, `badRequest`.

### 4. Unit-тест `AuthServiceRegisterTest`

- Mockito: `UserRepository`, `PasswordEncoder`
- Успешная регистрация STUDENT
- Дубликат логина → exception

## Критерии готовности

- [ ] STUDENT — публичная регистрация
- [ ] TEACHER — только через ADMIN
- [ ] Пароль хранится только как BCrypt-хэш
- [ ] Тесты зелёные

## Связанные задачи

- [12-controller-rest.md](./12-controller-rest.md)
- [11-security-config.md](./11-security-config.md)
