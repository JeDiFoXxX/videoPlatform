# 09 — SignalingController (REST API v1)

## Цель

`POST /api/v1/signaling` с `@Valid` DTO и ответом `SignalingResponseDto`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [05-service-storage-validation.md](./05-service-storage-validation.md) — validation starter
- [08-service-call-actions.md](./08-service-call-actions.md) — полный сервис

## Зависимости

Без новых в pom. Actuator — в [14](./14-makefile-ci.md).

## Шаги (Red → Green → Refactor)

### 1. Контроллер

**Файл:** `controller/SignalingController.java`

```java
@RestController
@RequestMapping("/api/v1")
public class SignalingController {

    private final SignalingService signalingService;

    @PostMapping("/signaling")
    public SignalingResponseDto signal(@Valid @RequestBody SignalingDto dto) {
        return signalingService.handle(dto);
    }
}
```

### 2. Ошибки Bean Validation

До [11](./11-exception-handler.md): Spring по умолчанию отдаёт 400 на `@Valid` failures. После 11 — единый `ErrorResponse`.

### 3. MockMvc-тесты

**Файл:** `controller/SignalingControllerIntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class SignalingControllerIntegrationTest { ... }
```

| # | Запрос | Ожидание |
|---|--------|----------|
| 1 | make_call | 200, `room_id`, `calling` |
| 2 | битый JSON | 400 |
| 3 | пустой sender_id | 400 |
| 4 | unknown type | 400 |
| 5 | make → accept | `active` |

### 4. Ручная проверка

```bash
curl -X POST http://localhost:8080/api/v1/signaling ^
  -H "Content-Type: application/json" ^
  -d "{\"type\":\"make_call\",\"sender_id\":\"t1\",\"target_id\":\"s1\"}"
```

## Критерии готовности

- [ ] Эндпоинт доступен
- [ ] `@Valid` на теле запроса
- [ ] MockMvc-тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingControllerIntegrationTest
.\mvnw.cmd spring-boot:run
```

## Связанные задачи

- [11-exception-handler.md](./11-exception-handler.md)
- [12-integration-tests.md](./12-integration-tests.md)
