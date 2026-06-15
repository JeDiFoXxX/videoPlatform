# 00 — Bootstrap и базовая конфигурация

## Цель

Подготовить каркас: Spring-контекст поднимается, единый набор тестовых зависимостей, базовый `application.properties` 
с настройками PostgreSQL.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- JDK 21, Maven wrapper в репозитории.

## Зависимости (`pom.xml`)

### Уже есть (не трогать)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Добавить: `spring-boot-starter-test`

**Зачем:** JUnit 5, AssertJ, Mockito, `SpringBootTest`, MockMvc — один согласованный BOM.

**Действие:**

1. Добавить:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

2. **Проверить конфликт** с существующими:
   - `spring-boot-starter-data-jpa-test`
   - `spring-boot-starter-security-test`
   - `spring-boot-starter-webmvc-test`

   Если после `mvn test` всё зелёное — **удалить** три раздельных test-стартерa, оставить только `spring-boot-starter-test`.

## Шаги (Red → Green → Refactor)

### 1. Главный класс

**Файл:** `src/main/java/ru/videoplatform/auth/AuthApplication.java`

```java
@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

### 2. `application.properties`

**Файл:** `src/main/resources/application.properties`

```properties
spring.application.name=auth
server.port=8081
logging.level.ru.videoplatform.auth=INFO

spring.datasource.url=jdbc:postgresql://localhost:5432/videoplatform_auth
spring.datasource.username=auth
spring.datasource.password=auth
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

`ddl-auto=validate` — схема управляется Flyway ([07](./07-repositories-flyway.md)).

### 3. Smoke-тест

**Файл:** `src/test/java/ru/videoplatform/auth/AuthApplicationTests.java`

```java
@SpringBootTest
class AuthApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Для smoke без PostgreSQL — `@SpringBootTest(properties = "spring.autoconfigure.exclude=...")` или Testcontainers в [13](./14-integration-tests.md).

## Критерии готовности

- [ ] `spring-boot-starter-test` добавлен; дубли test-стартеров убраны или обоснованы
- [ ] `mvn test` — `contextLoads` зелёный (с заглушкой БД или Testcontainers)
- [ ] `application.properties` содержит datasource и порт 8081

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=AuthApplicationTests -pl services/auth
```

## Связанные задачи

- **Следующая:** [01-user-role.md](./01-user-role.md)
- **Миграции:** [07-repositories-flyway.md](./07-repositories-flyway.md)
