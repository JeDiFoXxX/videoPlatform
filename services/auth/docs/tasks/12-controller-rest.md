# 12 — AuthController (REST API v1)

## Цель

HTTP-эндпоинты из [spec.md](../spec.md) с `@Valid` DTO.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [09-service-register.md](./09-service-register.md) — `AuthService` (register)
- [10-service-session.md](./10-service-session.md) — login / refresh / logout
- [11-security-config.md](./11-security-config.md) — security + JWT filter

## DTO (уже реализованы)

| Класс | Пакет | Валидация |
|-------|-------|-----------|
| `RegisterDto` | `dto.request` | `@NotBlank`, `@ValidPassword`, login rules |
| `TeacherRegisterDto` | `dto.request` | те же правила |
| `LoginDto` | `dto.request` | без `@ValidPassword` |
| `RefreshDto` | `dto.request` | — |
| `AuthResponseDto` | `dto.response` | defaults `type`, `expires_in` |

## Зависимости

Без новых. Actuator — в [15](./15-makefile-ci.md).

## Шаги (Red → Green → Refactor)

### 1. Контроллер

**Файл:** `controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterDto dto) {
        authService.registerStudent(dto);
    }

    @PostMapping("/register/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTeacher(@Valid @RequestBody TeacherRegisterDto dto) { ... }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginDto dto) { ... }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshDto dto) { ... }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RefreshDto dto) { ... }
}
```

### 2. MockMvc-тесты

**Файл:** `controller/AuthControllerIntegrationTest.java`

| # | Запрос | Ожидание |
|---|--------|----------|
| 1 | POST `/register` валидный | 201 |
| 2 | POST `/register` дубликат | 409 |
| 3 | POST `/login` (admin / `Admin123!!!`) | 200 + tokens |
| 4 | POST `/login` неверный пароль | 401 |
| 5 | POST `/refresh` | 200 + новый access |
| 6 | POST `/logout` | 204 |

## Критерии готовности

- [ ] Все 5 эндпоинтов из spec
- [ ] `@Valid` на телах запросов
- [ ] MockMvc-тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=AuthControllerIntegrationTest -pl services/auth
```

## Связанные задачи

- [13-exception-handler.md](./13-exception-handler.md)
- [14-integration-tests.md](./14-integration-tests.md)
- [15-makefile-ci.md](./15-makefile-ci.md)
