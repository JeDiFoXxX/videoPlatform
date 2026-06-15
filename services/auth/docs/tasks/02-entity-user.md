# 02 — User (сущность пользователя)

## Цель

JPA-сущность пользователя: логин, BCrypt-хэш пароля, роль.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [01-user-role.md](./01-user-role.md)

## Зависимости

Без новых — JPA уже в pom.

## Шаги (Red → Green → Refactor)

### 1. Entity `User`

**Файл:** `src/main/java/ru/videoplatform/auth/model/User.java`

| Поле | Тип | Ограничения |
|------|-----|-------------|
| `id` | `UUID` | `@Id`, `@GeneratedValue` |
| `login` | `String` | unique, max 20, латиница+цифры |
| `passwordHash` | `String` | BCrypt, не сериализовать наружу |
| `role` | `UserRole` | `@Enumerated(STRING)` |
| `createdAt` | `Instant` | `@CreationTimestamp` |

```java
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "login"))
public class User { ... }
```

### 2. Таблица (Flyway — [07](./07-repositories-flyway.md))

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    login VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 3. Unit-тест маппинга

**Файл:** `src/test/java/ru/videoplatform/auth/model/UserEntityTest.java`

- Сохранение и чтение через `@DataJpaTest` (H2 или Testcontainers — см. [13](./14-integration-tests.md)).

## Критерии готовности

- [ ] `User` в пакете `model`
- [ ] `login` unique, `role` — строка в БД
- [ ] Тест маппинга зелёный

## Связанные задачи

- [07-repositories-flyway.md](./07-repositories-flyway.md)
- [09-service-register.md](./09-service-register.md)
