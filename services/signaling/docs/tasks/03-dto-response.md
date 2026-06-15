# 03 — SignalingResponseDto (ответ API)

## Цель

Формат ответа REST: `room_id` (`@JsonProperty`) и `status`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [01-room-status.md](./01-room-status.md)

## Зависимости

Без новых. `status` — тип `RoomStatus` (рекомендуется) или `String`.

## Шаги (Red → Green → Refactor)

### 1. Класс

**Файл:** `src/main/java/ru/videoplatform/signaling/dto/SignalingResponseDto.java`

| Java | JSON |
|------|------|
| `roomId` | `room_id` |
| `status` | `status` |

Factory для сервиса:

```java
public static SignalingResponseDto of(String roomId, RoomStatus status) {
    return new SignalingResponseDto(roomId, status);
}
```

### 2. Примеры ответов

После `make_call`:

```json
{
  "room_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "calling"
}
```

После `accept_call`:

```json
{
  "room_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "active"
}
```

### 3. Тесты

**Файл:** `src/test/java/ru/videoplatform/signaling/dto/SignalingResponseDtoSerializationTest.java`

- В JSON ключ `room_id`, не `roomId`.
- `status` — `"calling"` / `"active"`.

## Критерии готовности

- [ ] `room_id` через `@JsonProperty("room_id")`
- [ ] `status` согласован с `RoomStatus`
- [ ] Тест сериализации зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingResponseDtoSerializationTest
```

## Связанные задачи

- [06-service-make-call.md](./06-service-make-call.md) … [08-service-call-actions.md](./08-service-call-actions.md)
