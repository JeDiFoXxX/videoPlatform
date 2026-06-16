# 01 — UserRole (роли пользователей)

## Цель

Типобезопасные роли платформы. В JWT и JSON — строки `"ADMIN"`, `"TEACHER"`, `"STUDENT"`.

## Шаги (Red → Green → Refactor)

### 1. Enum `UserRole`

**Файл:** `src/main/java/ru/videoplatform/auth/model/UserRole.java`

Wire value = имя константы — **`@JsonValue` / `@JsonCreator` не нужны**.

### 2. Unit-тест `UserRoleTest`

| Кейс | Ожидание |
|------|----------|
| `UserRole.valueOf("STUDENT")` | `STUDENT` |
| `UserRole.valueOf("UNKNOWN")` | `IllegalArgumentException` |
| Serialize `STUDENT` | `"STUDENT"` в JSON |
| Deserialize `"STUDENT"` | `UserRole.STUDENT` |

## Критерии готовности

- [ ] `UserRoleTest` зелёный (valueOf, negative, serialize, deserialize)

## Связанные задачи

- [02-entity-user.md](./02-entity-user.md)
