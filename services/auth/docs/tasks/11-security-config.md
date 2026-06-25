# 11 — SecurityConfig и JWT filter

## Цель

Настроить Spring Security: публичные эндпоинты, JWT-фильтр с проверкой blacklist, полный `SecurityFilterChain`.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | `SecurityConfigTest` — падает без filter chain |
| **Green** | `SecurityFilterChain`, `JwtAuthenticationFilter` |
| **Refactor** | Whitelist URL в константы |

## Предусловия

- [08-jwt-service.md](./08-jwt-service.md) — JJWT `JwtService`
- [10-service-session.md](./10-service-session.md) — logout + blacklist в БД

## Текущее состояние

Реализовано:

- `SecurityConfig` — `@EnableWebSecurity`, `@EnableMethodSecurity`, stateless session
- `JwtAuthenticationFilter` — Bearer JWT, blacklist, `ROLE_*` из claims
- `SecurityConfigTest` + `SecurityTestController` (stub для проверки security до task 12)

## Зависимости

Без новых — `spring-boot-starter-security` уже в pom.

## Конфигурация JWT

Уже в `application.properties` ([08](./08-jwt-service.md)):

```properties
app.security.jwt.secret=${JWT_SECRET:my-super-secret-key-32-symbols-minimum-length}
app.security.jwt.access-token-lifetime=900s
app.security.jwt.refresh-token-lifetime=30d
```

## Шаги (Red → Green → Refactor)

### 1. Расширить `SecurityConfig`

**Файл:** `config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. Публичные эндпоинты (permitAll)

| Метод | Путь |
|-------|------|
| POST | `/api/v1/auth/register` |
| POST | `/api/v1/auth/login` |
| POST | `/api/v1/auth/refresh` |
| GET | `/actuator/health` |

### 3. Защищённые

| Метод | Путь | Роль |
|-------|------|------|
| POST | `/api/v1/auth/register/teacher` | ADMIN |
| POST | `/api/v1/auth/logout` | authenticated |

### 4. `JwtAuthenticationFilter`

**Файл:** `config/JwtAuthenticationFilter.java`

| Шаг | Действие |
|-----|----------|
| 1 | Читать `Authorization: Bearer <token>` |
| 2 | `jwtService.isTokenValid(token)` — подпись + exp (JJWT) |
| 3 | `!blacklistedTokenRepository.existsByJti(jwtService.extractJti(token))` |
| 4 | Установить `SecurityContext` с authority `ROLE_<role>` из claims |

### 5. CSRF

Отключить для stateless REST API.

### 6. Тест `SecurityConfigTest`

| Кейс | Ожидание |
|------|----------|
| POST `/register` без токена | 201 |
| POST `/register/teacher` без токена | 403 |
| POST `/register/teacher` с ADMIN JWT | 201 |
| Запрос с blacklisted access | 401 |

## Критерии готовности

- [x] BCrypt `PasswordEncoder` bean
- [x] Явный `SecurityFilterChain` — **нет** generated security password в логах
- [x] JWT filter + blacklist на защищённых маршрутах
- [x] Роли ADMIN / TEACHER / STUDENT из JWT claims
- [x] `SecurityConfigTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SecurityConfigTest -pl services/auth
```

## Связанные задачи

- [12-controller-rest.md](./12-controller-rest.md)
- [14-integration-tests.md](./14-integration-tests.md)
