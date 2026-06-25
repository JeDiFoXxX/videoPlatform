# Задачи разработки auth

Пошаговый план v1 (REST + PostgreSQL + JWT) для компонента **videoPlatform**.  
Спецификация: [docs/spec.md](../spec.md).

## Методология TDD

Каждая задача выполняется циклом **Red → Green → Refactor**:

1. **Red** — написать падающий тест (`mvn test` должен упасть).
2. **Green** — минимальная реализация.
3. **Refactor** — улучшить код, тесты остаются зелёными.

Порядок: **сначала тесты, потом production-код**.

## Соглашения проекта

| Тема | Значение |
|------|----------|
| Базовый пакет | `ru.videoplatform.auth` |
| БД | `videoplatform_auth` |
| Поле хэша пароля | `passwordHash` → колонка `password_hash` |
| Миграции | **только Liquibase** (Flyway не используем) |
| JWT | **JJWT 0.13.0** (`io.jsonwebtoken`) |
| JWT config | `app.security.jwt.*` → `JwtProperties` |
| DTO | `dto.request` / `dto.response` |
| Dev admin | login `admin`, пароль `Admin123!!!` (BCrypt в Liquibase 004) |
| Security до задачи 11 | временный `Using generated security password` в логах — нормально |

## Зависимости Maven (по этапам)

| Зависимость | Задача | Назначение |
|-------------|--------|------------|
| *(уже в pom)* `spring-boot-starter-data-jpa` | — | JPA, PostgreSQL |
| *(уже в pom)* `spring-boot-starter-security` | 11 | Security filter chain |
| *(уже в pom)* `spring-boot-starter-webmvc` | — | REST API |
| *(уже в pom)* `postgresql` | — | Драйвер БД |
| `spring-boot-starter-test` | [00](./00-bootstrap.md) | Единый test-starter |
| `spring-boot-starter-liquibase` | [00](./00-bootstrap.md) | Миграции схемы БД |
| `h2` (test) | [00](./00-bootstrap.md) | In-memory БД для тестов |
| `spring-boot-starter-validation` | [06](./06-password-validation.md) | `jakarta.validation` на DTO |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.13.0 | [08](./08-jwt-service.md) | JWT |
| `spring-boot-starter-data-jpa-test` | [07](./07-repositories-liquibase.md) | `@DataJpaTest` |
| `testcontainers-postgresql` | [14](./14-integration-tests.md) | E2E |
| `spring-boot-starter-actuator` | [15](./15-makefile-ci.md) | Health для CI |

## Порядок v1

| # | Файл | Red (тест) | Green (код) |
|---|------|------------|-------------|
| 00 | [00-bootstrap.md](./00-bootstrap.md) | `AuthApplicationTests` | Config, Liquibase, test profile |
| 01 | [01-user-role.md](./01-user-role.md) | `UserRoleTest` | `UserRole` enum |
| 02 | [02-entity-user.md](./02-entity-user.md) | `UserTest` | `User` JPA |
| 03 | [03-entity-tokens.md](./03-entity-tokens.md) | `RefreshTokenTest`, `BlacklistedTokenTest` | Token entities + Liquibase 002–003 |
| 04 | [04-dto-request.md](./04-dto-request.md) | `DtoRequestTest` | Request DTO (`dto.request`) |
| 05 | [05-dto-response.md](./05-dto-response.md) | `AuthResponseDtoTest` | Response DTO (`dto.response`) |
| 06 | [06-password-validation.md](./06-password-validation.md) | `PasswordValidatorTest` | Validation |
| 07 | [07-repositories-liquibase.md](./07-repositories-liquibase.md) | `UserRepositoryTest` | Repos + seed ADMIN (004) |
| 08 | [08-jwt-service.md](./08-jwt-service.md) | `JwtServiceTest` | `JwtService` (JJWT) |
| 09 | [09-service-register.md](./09-service-register.md) | `AuthServiceTest` (register) | `AuthService.register*` |
| 10 | [10-service-session.md](./10-service-session.md) | `AuthServiceTest` (session) | login / refresh / logout |
| 11 | [11-security-config.md](./11-security-config.md) | `SecurityConfigTest` | `SecurityFilterChain` + JWT filter |
| 12 | [12-controller-rest.md](./12-controller-rest.md) | `AuthControllerIntegrationTest` | REST |
| 13 | [13-exception-handler.md](./13-exception-handler.md) | `GlobalExceptionHandlerTest` | Error JSON |
| 14 | [14-integration-tests.md](./14-integration-tests.md) | `AuthIntegrationTest` | E2E |
| 15 | [15-makefile-ci.md](./15-makefile-ci.md) | `ActuatorHealthIntegrationTest` | Docker, CI |

## Статус

| Задача | Статус |
|--------|--------|
| 00–08 | выполнено |
| 09–10 | выполнено (`AuthService` + `AuthServiceTest`) |
| 11 | выполнено (`SecurityFilterChain`, `JwtAuthenticationFilter`, `SecurityConfigTest`) |
| 12–15 | не начато |
