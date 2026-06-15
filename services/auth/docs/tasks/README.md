# Задачи разработки auth

Пошаговый план v1 (REST + PostgreSQL + JWT) для компонента **videoPlatform**.  
Спецификация: [docs/spec.md](../spec.md).

## Методология TDD

Каждая задача выполняется циклом **Red → Green → Refactor**:

1. **Red** — написать падающий тест под критерии задачи (`mvn test` должен упасть).
2. **Green** — минимальная реализация, чтобы тест прошёл.
3. **Refactor** — улучшить код, не ломая тесты.

Порядок внутри задачи: **сначала тесты, потом production-код**.  
Интеграционные тесты ([14](./14-integration-tests.md)) — после unit/MockMvc тестов слоёв.

Базовый пакет: `ru.videoplatform.auth`.

## Зависимости Maven (по этапам)

| Зависимость | Задача | Назначение |
|-------------|--------|------------|
| *(уже в pom)* `spring-boot-starter-data-jpa` | — | JPA, PostgreSQL |
| *(уже в pom)* `spring-boot-starter-security` | 11 | Security filter chain |
| *(уже в pom)* `spring-boot-starter-webmvc` | — | REST API |
| *(уже в pom)* `postgresql` | — | Драйвер БД |
| `spring-boot-starter-test` | [00](./00-bootstrap.md) | Единый test-starter |
| `spring-boot-starter-validation` | [06](./06-password-validation.md) | `jakarta.validation` на DTO |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | [08](./08-jwt-service.md) | Генерация и разбор JWT |
| `flyway-core` + `flyway-database-postgresql` | [07](./07-repositories-flyway.md) | Миграции схемы БД |
| `testcontainers-postgresql` | [14](./14-integration-tests.md) | E2E с реальной PostgreSQL |
| `spring-boot-starter-actuator` | [15](./15-makefile-ci.md) | `/actuator/health` для Docker/CI |

## Порядок v1

| # | Файл | Red (тест) | Green (код) |
|---|------|------------|-------------|
| 00 | [00-bootstrap.md](./00-bootstrap.md) | `AuthApplicationTests` | Конфиг, test-starter |
| 01 | [01-user-role.md](./01-user-role.md) | `UserRoleTest` | `UserRole` enum |
| 02 | [02-entity-user.md](./02-entity-user.md) | `UserEntityTest` | `User` JPA |
| 03 | [03-entity-tokens.md](./03-entity-tokens.md) | `RefreshTokenTest` | Token entities |
| 04 | [04-dto-request.md](./04-dto-request.md) | `RegisterDtoTest` | Request DTO |
| 05 | [05-dto-response.md](./05-dto-response.md) | `AuthResponseDtoTest` | Response DTO |
| 06 | [06-password-validation.md](./06-password-validation.md) | `PasswordValidatorTest` | Validation rules |
| 07 | [07-repositories-flyway.md](./07-repositories-flyway.md) | `UserRepositoryTest` | Flyway + repos |
| 08 | [08-jwt-service.md](./08-jwt-service.md) | `JwtServiceTest` | `JwtService` |
| 09 | [09-service-register.md](./09-service-register.md) | `AuthServiceRegisterTest` | register |
| 10 | [10-service-session.md](./10-service-session.md) | `AuthServiceSessionTest` | login/refresh/logout |
| 11 | [11-security-config.md](./11-security-config.md) | `SecurityConfigTest` | JWT filter chain |
| 12 | [12-controller-rest.md](./12-controller-rest.md) | `AuthControllerIntegrationTest` | REST controller |
| 13 | [13-exception-handler.md](./13-exception-handler.md) | `GlobalExceptionHandlerTest` | Error JSON |
| 14 | [14-integration-tests.md](./14-integration-tests.md) | `AuthIntegrationTest` | E2E Testcontainers |
| 15 | [15-makefile-ci.md](./15-makefile-ci.md) | `ActuatorHealthIntegrationTest` | Docker, CI |

## Статус

| Задача | Статус |
|--------|--------|
| 00–15 | не начато |
