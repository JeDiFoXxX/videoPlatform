# 15 — Actuator, Makefile, Docker, CI

## Цель

Продакшен-готовность: health endpoint, единые команды, Docker-образ с PostgreSQL.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | `ActuatorHealthIntegrationTest` — падает без actuator |
| **Green** | Подключить starter, Makefile, Dockerfile |
| **Refactor** | Вынести общие CI-шаги в корневой workflow |

## Предусловия

- [14-integration-tests.md](./14-integration-tests.md) — `mvn test` зелёный

## Зависимости (`pom.xml`)

### Добавить: `spring-boot-starter-actuator`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Конфигурация

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized
```

## Шаги (Red → Green → Refactor)

### Green — инфраструктура

**Makefile**, **Dockerfile**, **docker-compose.yml**:

- `auth` — порт 8081
- `postgres` — БД `videoplatform_auth`
- `docker build -t videoplatform/auth:local .`

Корневой `.github/workflows/maven.yml` — `./mvnw verify`.

## Сводка зависимостей по всему плану

| Starter / lib | Задача |
|---------------|--------|
| `spring-boot-starter-test` | 00 |
| `spring-boot-starter-liquibase` | 00 |
| `spring-boot-starter-validation` | 06 |
| `jjwt-*` 0.13.0 | 08 |
| `spring-boot-starter-data-jpa-test` | 07 |
| `testcontainers-postgresql` | 14 |
| `spring-boot-starter-actuator` | **15** |

## Критерии готовности

- [ ] `/actuator/health` → UP
- [ ] `docker build` успешен
- [ ] GitHub Actions зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=ActuatorHealthIntegrationTest -pl services/auth
docker build -t videoplatform/auth:local services/auth
curl http://localhost:8081/actuator/health
```

## Связанные задачи

- [00-bootstrap.md](./00-bootstrap.md) … [14-integration-tests.md](./14-integration-tests.md)
