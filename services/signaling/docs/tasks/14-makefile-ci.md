# 14 — Actuator, Makefile, Docker, CI

## Цель

Продакшен-готовность локальной и CI-сборки: health endpoint, единые команды, Docker-образ.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [12-integration-tests.md](./12-integration-tests.md) — `mvn test` зелёный

## Зависимости (`pom.xml`)

### Добавить: `spring-boot-starter-actuator`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Jakarta / отдельные артефакты не нужны.**

### Конфигурация

**Файл:** `src/main/resources/application.properties` (дополнить)

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized
```

Для Docker/K8s probes достаточно:

```
GET /actuator/health → {"status":"UP"}
```

Опционально позже: `metrics`, `prometheus` — отдельная задача.

## Шаги (Red → Green → Refactor)

### 1. Makefile

**Файл:** `Makefile`

| Target | Команда |
|--------|---------|
| `help` | список целей |
| `test` | `./mvnw test` |
| `verify` | `./mvnw verify` |
| `run` | `./mvnw spring-boot:run` |
| `build` | `./mvnw -DskipTests package` |
| `docker-build` | `docker build -t videoplatform/signaling:local .` |
| `docker-run` | run + port 8080 |

Windows: документировать `.\mvnw.cmd` в README.

### 2. Dockerfile

Multi-stage:

1. `eclipse-temurin:21-jdk` — `mvnw package -DskipTests`
2. `eclipse-temurin:21-jre` — `java -jar app.jar`

**HEALTHCHECK** (опционально):

```dockerfile
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### 3. `.dockerignore`

```
target/
.git/
.idea/
```

### 4. `docker-compose.yml` (опционально)

Сервис `signaling` + заглушка `auth` для локальной сети.

### 5. GitHub Actions

**Файл:** `.github/workflows/maven.yml`

- JDK 21
- cache Maven
- `./mvnw verify`
- (опционально) job `docker build` на main

### 6. Checkstyle

В репозитории есть `checkstyle.xml` — подключить plugin в `pom.xml` фаза `verify` или цель `make lint`.

### 7. README

- Быстрый старт: `make run`
- curl пример v1
- health: `curl http://localhost:8080/actuator/health`
- ссылка на `docs/spec.md`, `docs/tasks/`

### 8. Тест actuator

**Файл:** `ActuatorHealthIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ActuatorHealthIntegrationTest {
    @Test
    void healthIsUp() {
        // RestClient или TestRestTemplate → /actuator/health → UP
    }
}
```

## Сводка зависимостей по всему плану

| Starter | Задача |
|---------|--------|
| `spring-boot-starter-test` | 00 |
| `spring-boot-starter-validation` | 05 |
| `spring-boot-starter-actuator` | **14** |
| `spring-boot-starter-oauth2-resource-server` | **13** (опционально) |
| webmvc, websocket | уже в проекте |

`@EnableScheduling` — только **10**.

## Критерии готовности

- [ ] `spring-boot-starter-actuator` в pom
- [ ] `/actuator/health` → UP при запущенном приложении
- [ ] `make test` / `mvnw test` зелёный
- [ ] `docker build` успешен
- [ ] GitHub Actions зелёный
- [ ] README обновлён

## Порядок запуска локально

1. `git clone` && `cd signaling`
2. `make test` или `.\mvnw.cmd test`
3. `make run`
4. curl `POST /api/v1/signaling`
5. curl `GET /actuator/health`
6. `make docker-run` (опционально)

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=ActuatorHealthIntegrationTest
.\mvnw.cmd verify
docker build -t videoplatform/signaling:local .
curl http://localhost:8080/actuator/health
```

## Связанные задачи

- [00-bootstrap.md](./00-bootstrap.md) … [13-websocket-auth.md](./13-websocket-auth.md)
