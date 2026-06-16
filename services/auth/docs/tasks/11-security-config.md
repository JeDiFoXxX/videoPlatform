# 11 — SecurityConfig и JWT filter

## Цель

Настроить Spring Security: публичные эндпоинты, JWT-фильтр для защищённых, BCrypt `PasswordEncoder`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [08-jwt-service.md](./08-jwt-service.md)
- [10-service-session.md](./10-service-session.md)

## Зависимости

Без новых — `spring-boot-starter-security` уже в pom.

> **До этой задачи:** Spring Security создаёт временный `UserDetailsService` и пишет в лог  
> `Using generated security password: ...`. После настройки явного `SecurityFilterChain` это исчезает.

## Шаги (Red → Green → Refactor)

### 1. `SecurityConfig`

**Файл:** `config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) { ... }

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

**Файл:** `security/JwtAuthenticationFilter.java`

- Читает `Authorization: Bearer <token>`
- `jwtService.isTokenValid` + проверка blacklist
- Устанавливает `SecurityContext` с `role` из claims

### 5. CSRF

Отключить для stateless REST API (`csrf.disable()`).

### 6. Тест `SecurityConfigTest`

- `POST /register` без токена → 200/201
- `POST /register/teacher` без токена → 403
- `POST /register/teacher` с ADMIN JWT → 201

## Критерии готовности

- [ ] Явный `SecurityFilterChain` — **нет** generated security password в логах
- [ ] BCrypt bean зарегистрирован
- [ ] JWT filter на защищённых маршрутах
- [ ] Роли ADMIN/TEACHER/STUDENT из claims
- [ ] Security-тесты зелёные

## Связанные задачи

- [12-controller-rest.md](./12-controller-rest.md)
- [14-integration-tests.md](./14-integration-tests.md)
