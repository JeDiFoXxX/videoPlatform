# 13 — GlobalExceptionHandler и ErrorResponse

## Цель

Единый JSON для ошибок API: 400 (validation + business), 401, 403, 409, 500.

## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | MockMvc-тесты на формат `ErrorResponse` и HTTP-код |
| **Green** | `GlobalExceptionHandler`, `ErrorResponse` |
| **Refactor** | Общий factory `ErrorResponse.of(...)` |

## Предусловия

- [12-controller-rest.md](./12-controller-rest.md)

## Текущее состояние

Сейчас `AuthException` extends `ResponseStatusException` ([09](./09-service-register.md)) — Spring отдаёт стандартный JSON ошибки.  
В этой задаче — **единый** формат ответа для всех ошибок auth API.

## Зависимости

Без новых. `MethodArgumentNotValidException` — validation starter ([06](./06-password-validation.md)).

## Шаги (Red → Green → Refactor)

### Red — тесты первыми

**Файл:** `src/test/java/ru/videoplatform/auth/exception/GlobalExceptionHandlerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void validationErrorReturns400WithErrorResponse() throws Exception { ... }

    @Test
    void conflictReturns409WithErrorResponse() throws Exception { ... }

    @Test
    void unauthorizedReturns401WithErrorResponse() throws Exception { ... }
}
```

| Сценарий | HTTP | Поля JSON |
|----------|------|-----------|
| Слабый пароль / невалидный register | 400 | status, error, message, timestamp |
| Дубликат логина | 409 | status, error, message, timestamp |
| Неверный пароль / login | 401 | status, error, message, timestamp |
| Нет прав (teacher без ADMIN) | 403 | status, error, message, timestamp |

Команда Red: `mvn test -Dtest=GlobalExceptionHandlerTest` — **должен упасть**.

### Green — реализация

**Файл:** `exception/ErrorResponse.java`

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "timestamp": "2026-06-15T12:00:00Z"
}
```

**Файл:** `exception/GlobalExceptionHandler.java`

| Исключение | HTTP |
|------------|------|
| `AuthException` | status из exception |
| `MethodArgumentNotValidException` | 400 |
| `AccessDeniedException` | 403 |
| `ResponseStatusException` | соответствующий status |

**Refactor `AuthException`:** оставить factories (`conflict`, `unauthorized`, `badRequest`), обрабатывать в handler — не дублировать ad-hoc `ResponseEntity` в контроллере.

### Refactor

- Статический метод `ErrorResponse.of(HttpStatus status, String message)`
- Stack trace **не** отдаётся клиенту
- Сообщения validation — первое поле или список (на выбор, зафиксировать в тестах)

## Критерии готовности

- [ ] Red-тесты покрывают основные сценарии v1
- [ ] Все ошибки auth API — единый JSON `ErrorResponse`
- [ ] `GlobalExceptionHandlerTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=GlobalExceptionHandlerTest -pl services/auth
```

## Связанные задачи

- [14-integration-tests.md](./14-integration-tests.md)
