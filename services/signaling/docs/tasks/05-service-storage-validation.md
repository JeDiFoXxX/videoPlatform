# 05 — SignalingService: хранилище, validation starter, валидация

## Цель

`ConcurrentHashMap` для комнат, валидация DTO. **На этом этапе** подключить `spring-boot-starter-validation`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [02-dto-signaling.md](./02-dto-signaling.md)
- [04-model-call-room.md](./04-model-call-room.md)

## Зависимости (`pom.xml`)

### Добавить

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Jakarta:** отдельно `jakarta.validation-api` **не** добавлять — приходит транзитивно.

### Аннотации на DTO (обновить [02](./02-dto-signaling.md))

```java
public class SignalingDto {

    @NotBlank
    @JsonProperty("sender_id")
    private String senderId;

    @NotBlank
    @JsonProperty("target_id")
    private String targetId;

    @NotBlank
    @JsonProperty("type")
    private String type;

    @JsonProperty("room_id")
    private String roomId;
}
```

В контроллере (задача 09): `@Valid @RequestBody SignalingDto dto`.

## Шаги (Red → Green → Refactor)

### 1. `SignalingService` (каркас)

**Файл:** `service/SignalingService.java`

```java
@Service
public class SignalingService {
    private final ConcurrentHashMap<String, CallRoom> rooms = new ConcurrentHashMap<>();

    public SignalingResponseDto handle(SignalingDto dto) {
        validateBusinessRules(dto);
        throw new UnsupportedOperationException("Not implemented: " + dto.getType());
    }
}
```

### 2. Бизнес-валидация (помимо Bean Validation)

| Правило | HTTP (позже в 11) |
|---------|-------------------|
| `senderId.equals(targetId)` | 400 |
| `type` не из whitelist | 400 |
| `room_id` обязателен для accept/reject/leave | 400 |

Whitelist: `make_call`, `accept_call`, `reject_call`, `leave_room`.

### 3. Вспомогательные методы

- `Optional<CallRoom> findRoom(String roomId)`
- `void saveRoom(CallRoom room)`
- `void removeRoom(String roomId)`

### 4. Unit-тесты

**Файл:** `service/SignalingServiceValidationTest.java` (без Spring)

| Кейс | Ожидание |
|------|----------|
| Пустой sender/target/type | исключение |
| sender == target | 400 |
| unknown type | 400 |
| accept без room_id | 400 |

**Файл:** `dto/SignalingDtoValidationTest.java` (опционально)

- `@WebMvcTest` или `Validator` bean: пустой `sender_id` → constraint violation.

## Критерии готовности

- [ ] `spring-boot-starter-validation` в pom
- [ ] `@NotBlank` на обязательных полях DTO
- [ ] `ConcurrentHashMap` в сервисе
- [ ] Unit-тесты валидации зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingServiceValidationTest
```

## Связанные задачи

- [06-service-make-call.md](./06-service-make-call.md)
- [11-exception-handler.md](./11-exception-handler.md)
