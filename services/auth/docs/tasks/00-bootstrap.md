# 00 — Bootstrap и базовая конфигурация

## Цель

Подготовить каркас: Spring-контекст поднимается, единый набор тестовых зависимостей, базовый `application.properties` 
с настройками PostgreSQL и Liquibase.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- JDK 21, Maven wrapper в репозитории.

## Соглашения проекта

| Тема | Значение |
|------|----------|
| Базовый пакет | `ru.videoplatform.auth` |
| БД (prod/dev) | `videoplatform_auth` |
| Миграции | **только Liquibase** (Flyway не используем) |
| Security (до [11](./11-security-config.md)) | Spring Boot генерирует временный пароль в лог — это нормально до явного `SecurityFilterChain` |

## Зависимости (`pom.xml`)

### Добавить

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-liquibase</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**Не добавлять** отдельный `liquibase-core` — он транзитивно приходит из `spring-boot-starter-liquibase`.

## Шаги (Red → Green → Refactor)

### 1. Главный класс

**Файл:** `src/main/java/ru/videoplatform/auth/AuthApplication.java`

### 2. `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/videoplatform_auth
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
spring.jpa.hibernate.ddl-auto=validate
```

### 3. Тестовый профиль

**Файл:** `src/test/resources/application-test.properties` — H2 + Liquibase.

**Smoke-тест:** `@SpringBootTest` + `@ActiveProfiles("test")`.

### 4. Security (временно)

До [11](./11-security-config.md) в логах: `Using generated security password: ...` — ожидаемо.

## Критерии готовности

- [ ] `spring-boot-starter-liquibase` без дублирующего `liquibase-core`
- [ ] `videoplatform_auth`, профиль `test` + `application-test.properties`
- [ ] `mvn test` — `contextLoads` зелёный

## Связанные задачи

- [01-user-role.md](./01-user-role.md)
- [07-repositories-liquibase.md](./07-repositories-liquibase.md)
