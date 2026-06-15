# 02 — SignalingDto (входящий запрос)

## Цель

DTO для `POST /api/v1/signaling`: `sender_id`, `target_id`, `type` (+ опционально `room_id`) через `@JsonProperty`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [00-bootstrap.md](./00-bootstrap.md)
- [01-room-status.md](./01-room-status.md) (для согласования терминов)

## Зависимости

**Не добавлять** `spring-boot-starter-validation` здесь — в [05](./05-service-storage-validation.md).  
На этом этапе достаточно Jackson из webmvc.

## Шаги (Red → Green → Refactor)

### 1. Класс `SignalingDto`

**Файл:** `src/main/java/ru/videoplatform/signaling/dto/SignalingDto.java`

| Java | JSON | Обязательность |
|------|------|----------------|
| `senderId` | `sender_id` | всегда |
| `targetId` | `target_id` | всегда |
| `type` | `type` | всегда |
| `roomId` | `room_id` | для `accept_call`, `reject_call`, `leave_room` |

### 2. Контракт `type` (v1)

| type | Описание |
|------|----------|
| `make_call` | Создать комнату |
| `accept_call` | Принять |
| `reject_call` | Отклонить |
| `leave_room` | Завершить |

### 3. Пример JSON

```json
{
  "type": "make_call",
  "sender_id": "teacher-123",
  "target_id": "student-321"
}
```

### 4. Тесты сериализации

**Файл:** `src/test/java/ru/videoplatform/signaling/dto/SignalingDtoSerializationTest.java`

- Десериализация snake_case → camelCase.
- `make_call` без `room_id`.
- `accept_call` с `room_id`.

**Подготовка к задаче 05:** оставить TODO-комментарий или пустые аннотации `@NotBlank` — **не** подключать validation до [05](./05-service-storage-validation.md).

## Критерии готовности

- [ ] Класс в `dto`, три поля с `@JsonProperty` по ТЗ
- [ ] Опциональный `room_id`
- [ ] `SignalingDtoSerializationTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingDtoSerializationTest
```

## Связанные задачи

- [03-dto-response.md](./03-dto-response.md)
- [05-service-storage-validation.md](./05-service-storage-validation.md)
- [09-controller-rest.md](./09-controller-rest.md)
