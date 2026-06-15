# 11 — GlobalExceptionHandler и ErrorResponse

## Цель

Единый JSON для ошибок: 400 (validation + бизнес), 404, 409, 500.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [05-service-storage-validation.md](./05-service-storage-validation.md)
- [07-service-conflicts.md](./07-service-conflicts.md)
- [09-controller-rest.md](./09-controller-rest.md)

## Зависимости

Без новых. `MethodArgumentNotValidException` — из validation starter (задача 05).

## Шаги (Red → Green → Refactor)

### 1. `SignalingException`

**Файл:** `exception/SignalingException.java`

```java
public class SignalingException extends RuntimeException {
    private final HttpStatus status;
    // factories: badRequest, notFound, conflict, forbidden
}
```

### 2. `ErrorResponse`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found: abc",
  "timestamp": "2026-05-16T12:00:00"
}
```

### 3. `@RestControllerAdvice`

| Исключение | HTTP |
|------------|------|
| `SignalingException` | из исключения |
| `MethodArgumentNotValidException` | 400 |
| `HttpMessageNotReadableException` | 400 |
| `Exception` | 500 (без stack trace в теле) |

### 4. Рефакторинг `SignalingService`

Заменить `IllegalArgumentException` на `SignalingException`.

### 5. Тесты MockMvc

**Файл:** `controller/SignalingExceptionHandlerTest.java`

- пустой sender → 400 + message
- accept без комнаты → 404
- дубликат make_call → 409

## Критерии готовности

- [ ] Все ожидаемые ошибки v1 → корректный HTTP
- [ ] MockMvc-тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingExceptionHandlerTest
```

## Связанные задачи

- [12-integration-tests.md](./12-integration-tests.md)
