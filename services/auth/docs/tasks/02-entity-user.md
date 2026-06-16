# 02 — User (сущность пользователя)

## Цель

JPA-сущность пользователя: логин, BCrypt-хэш пароля (`passwordHash`), роль.

## Шаги (Red → Green → Refactor)

### 1. Entity `User`

| Поле | Колонка |
|------|---------|
| `passwordHash` | `password_hash` |

> `@JsonIgnore` на `passwordHash` — см. [05](./05-dto-response.md) / [12](./12-controller-rest.md).

### 2. Liquibase changeset для `users`

Схема через Liquibase ([00](./00-bootstrap.md)), не Flyway.

### 3. Тесты `UserTest`

| Кейс | Ожидание |
|------|----------|
| persist + read | все поля, включая `passwordHash` |
| duplicate login | `PersistenceException` |
| login длиной 21 | `PersistenceException` |

## Критерии готовности

- [ ] Поле Java: **`passwordHash`**
- [ ] Тесты persist / duplicate / length зелёные

## Связанные задачи

- [07-repositories-liquibase.md](./07-repositories-liquibase.md)
