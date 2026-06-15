# 01 — UserRole (роли пользователей)

## Цель

Типобезопасные роли платформы. В JWT и JSON — строки `"ADMIN"`, `"TEACHER"`, `"STUDENT"`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [00-bootstrap.md](./00-bootstrap.md) выполнена.

## Зависимости

Новых зависимостей **не** добавлять. Jackson для enum — из `spring-boot-starter-webmvc`.

## Шаги (Red → Green → Refactor)

### 1. Enum `UserRole`

**Файл:** `src/main/java/ru/videoplatform/auth/model/UserRole.java`

| Constant | Wire value | Назначение |
|----------|------------|------------|
| `ADMIN` | `ADMIN` | Управление системой |
| `TEACHER` | `TEACHER` | Преподаватель |
| `STUDENT` | `STUDENT` | Ученик (роль по умолчанию при регистрации) |

```java
public enum UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}
```

Для JPA: `@Enumerated(EnumType.STRING)`.

### 2. Unit-тест

**Файл:** `src/test/java/ru/videoplatform/auth/model/UserRoleTest.java`

| Кейс | Ожидание |
|------|----------|
| `UserRole.valueOf("STUDENT")` | `STUDENT` |
| Сериализация через `ObjectMapper` | `"STUDENT"` в JSON |

## Критерии готовности

- [ ] Enum в пакете `model`
- [ ] Роли совпадают со [spec.md](../spec.md)
- [ ] `UserRoleTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=UserRoleTest -pl services/auth
```

## Связанные задачи

- [02-entity-user.md](./02-entity-user.md)
- [08-jwt-service.md](./08-jwt-service.md)
