# 07 — Repositories и Flyway-миграции

## Цель

Spring Data JPA repositories, миграции схемы, seed первого ADMIN.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [02-entity-user.md](./02-entity-user.md)
- [03-entity-tokens.md](./03-entity-tokens.md)

## Зависимости (`pom.xml`)

### Добавить

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### Конфигурация

```properties
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```

## Шаги (Red → Green → Refactor)

### 1. Repositories

**Файлы:**

- `repository/UserRepository.java` — `Optional<User> findByLogin(String login)`, `boolean existsByLogin`
- `repository/RefreshTokenRepository.java` — `Optional<RefreshToken> findByTokenAndRevokedFalse`
- `repository/BlacklistedTokenRepository.java` — `boolean existsByJti`

### 2. Flyway `V1__init_schema.sql`

Таблицы: `users`, `refresh_tokens`, `blacklisted_tokens`.

### 3. Flyway `V2__seed_admin.sql`

Первая учётная запись ADMIN (логин/пароль из env или фиксированный dev-пароль, хэш BCrypt).

```sql
INSERT INTO users (id, login, password_hash, role, created_at)
VALUES ('...', 'admin', '$2a$...', 'ADMIN', now());
```

Пароль — **не** хранить в открытом виде в репозитории; для dev — документировать в README.

### 4. `@DataJpaTest`

**Файл:** `UserRepositoryTest.java` — `findByLogin`, `existsByLogin`.

## Критерии готовности

- [ ] Flyway применяет миграции при старте
- [ ] ADMIN создан после миграции
- [ ] Repository-тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=UserRepositoryTest -pl services/auth
```

## Связанные задачи

- [09-service-register.md](./09-service-register.md)
- [14-integration-tests.md](./14-integration-tests.md)
