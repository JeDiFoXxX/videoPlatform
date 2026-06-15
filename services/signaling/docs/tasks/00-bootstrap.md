# 00 — Bootstrap и базовая конфигурация

## Цель

Подготовить каркас: Spring-контекст поднимается, единый набор тестовых зависимостей, базовый `application.properties`. 


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |
**Без** `@EnableScheduling` (перенесено в [10-scheduler-cleanup.md](./10-scheduler-cleanup.md)).

## Предусловия

- JDK 21, Maven wrapper в репозитории.

## Зависимости (`pom.xml`)

### Уже есть (не трогать)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### Добавить: `spring-boot-starter-test` (если необходим)

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
   - `spring-boot-starter-webmvc-test`
   - `spring-boot-starter-websocket-test`

   Если после `mvn test` всё зелёное — **удалить** два раздельных test-стартерa, оставить только `spring-boot-starter-test`.  
   Если WebSocket-тесты в [13](./13-websocket-auth.md) требуют `websocket-test` — оставить оба: `starter-test` + `websocket-test` (зафиксировать выбор в README задачи 13).

3. Jakarta в pom **не** добавлять — приходит транзитивно.

## Шаги (Red → Green → Refactor)

### 1. Главный класс

**Файл:** `src/main/java/ru/videoplatform/signaling/SignalingApplication.java`

```java
@SpringBootApplication
public class SignalingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignalingApplication.class, args);
    }
}
```

**Не добавлять** `@EnableScheduling` здесь.

### 2. `application.properties`

**Файл:** `src/main/resources/application.properties`

```properties
spring.application.name=signaling
server.port=8080
logging.level.ru.videoplatform.signaling=INFO
```

### 3. Smoke-тест

**Файл:** `src/test/java/ru/videoplatform/signaling/SignalingApplicationTests.java`

```java
@SpringBootTest
class SignalingApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Тест `schedulingIsEnabled` **не** писать в этой задаче — scheduling включается в [10](./10-scheduler-cleanup.md).

## Критерии готовности

- [ ] `spring-boot-starter-test` добавлен; дубли test-стартеров убраны или обоснованы
- [ ] `mvn test` — `contextLoads` зелёный
- [ ] `mvn spring-boot:run` — приложение стартует на 8080
- [ ] На `SignalingApplication` нет `@EnableScheduling`

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingApplicationTests
.\mvnw.cmd spring-boot:run
```

## Связанные задачи

- **Следующая:** [01-room-status.md](./01-room-status.md)
- **Validation:** [05-service-storage-validation.md](./05-service-storage-validation.md)
- **Scheduling:** [10-scheduler-cleanup.md](./10-scheduler-cleanup.md)
