# 14 — Сквозные интеграционные тесты

## Цель

E2E-сценарии auth через Spring-контекст и Testcontainers PostgreSQL.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Описать сценарии в `AuthIntegrationTest` (падают до полной сборки) |
| **Green** | Довести сервис/контроллер/security до прохождения сценариев |
| **Refactor** | Общие хелперы: `registerAndLogin()`, `@DynamicPropertySource` |

## Предусловия

- [13-exception-handler.md](./13-exception-handler.md)
- [12-controller-rest.md](./12-controller-rest.md)

## Зависимости (`pom.xml`)

### Добавить (test)

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## Шаги (Red → Green → Refactor)

### Red — тесты первыми

**Файл:** `src/test/java/ru/videoplatform/auth/AuthIntegrationTest.java`

| Сценарий | Шаги | Ожидание |
|----------|------|----------|
| Полный цикл | register → login → refresh → logout | 201/200/204 |
| Teacher by admin | login admin → register teacher → teacher login | 201/200 |
| Blacklist | logout → старый access на защищённый endpoint | 401 |

### Green — довести реализацию

- Liquibase changesets применяются в Testcontainers PostgreSQL
- Seed ADMIN из [07](./07-repositories-liquibase.md) доступен в тестах
- Все сценарии зелёные

### Refactor

**Файл:** `src/test/java/ru/videoplatform/auth/support/AuthTestClient.java`

## Критерии готовности

- [ ] Testcontainers PostgreSQL в CI
- [ ] Минимум 3 E2E сценария
- [ ] `mvn test -pl services/auth` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=AuthIntegrationTest -pl services/auth
```

## Связанные задачи

- [15-makefile-ci.md](./15-makefile-ci.md)
