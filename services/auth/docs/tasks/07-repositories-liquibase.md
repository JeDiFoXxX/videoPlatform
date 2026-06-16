# 07 — Repositories и Liquibase-миграции

## Цель

Spring Data JPA repositories, changesets для token-таблиц, seed первого ADMIN.

> **Примечание:** таблица `users` и Liquibase bootstrap уже созданы в [00](./00-bootstrap.md) / [02](./02-entity-user.md).  
> В этой задаче — repositories + changesets для `refresh_tokens`, `blacklisted_tokens` + seed ADMIN.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | `UserRepositoryTest` — падает без repository/changesets |
| **Green** | Repositories + Liquibase changesets |
| **Refactor** | Общие test-fixtures для seed ADMIN |

## Предусловия

- [02-entity-user.md](./02-entity-user.md)
- [03-entity-tokens.md](./03-entity-tokens.md)
- Liquibase уже в pom ([00](./00-bootstrap.md)): `spring-boot-starter-liquibase`

## Зависимости (`pom.xml`)

Дополнительных зависимостей **не** добавлять — достаточно `spring-boot-starter-liquibase` из задачи 00.

### Конфигурация (уже есть)

```properties
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.jpa.hibernate.ddl-auto=validate
```

## Шаги (Red → Green → Refactor)

### 1. Repositories

**Файлы:**

- `repository/UserRepository.java` — `Optional<User> findByLogin(String login)`, `boolean existsByLogin`
- `repository/RefreshTokenRepository.java` — `Optional<RefreshToken> findByTokenAndRevokedFalse`
- `repository/BlacklistedTokenRepository.java` — `boolean existsByJti`

### 2. Liquibase `002_ddl_create_token_tables.sql`

Таблицы: `refresh_tokens`, `blacklisted_tokens` (FK `refresh_tokens.user_id → users.id`).

Подключить в `db.changelog-master.xml`.

### 3. Liquibase `003_seed_admin.sql`

Первая учётная запись ADMIN (логин/пароль из env или фиксированный dev-пароль, хэш BCrypt).

```sql
INSERT INTO users (id, login, password_hash, role, created_at)
VALUES ('...', 'admin', '$2a$...', 'ADMIN', now());
```

Пароль — **не** хранить в открытом виде в репозитории; для dev — документировать в README.

### 4. `@DataJpaTest`

**Файл:** `UserRepositoryTest.java` — `findByLogin`, `existsByLogin`.

## Критерии готовности

- [ ] Liquibase применяет changesets при старте
- [ ] ADMIN создан после seed-changeset
- [ ] Repository-тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=UserRepositoryTest -pl services/auth
```

## Связанные задачи

- [09-service-register.md](./09-service-register.md)
- [14-integration-tests.md](./14-integration-tests.md)
