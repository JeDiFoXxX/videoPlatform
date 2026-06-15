# 08 — SignalingService: accept, reject, leave

## Цель

Завершить `handle()` для всех четырёх `type`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [06-service-make-call.md](./06-service-make-call.md)
- [07-service-conflicts.md](./07-service-conflicts.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. `accept_call`

1. Валидация + `room_id`
2. findRoom → 404
3. status == CALLING, sender == studentId
4. `room.withStatus(ACTIVE)` → put
5. Ответ: `active`

### 2. `reject_call`

Рекомендация v1: `rooms.remove(roomId)`, ответ `{ room_id, status: "rejected" }`.

### 3. `leave_room`

1. findRoom, assertParticipant
2. remove
3. Ответ: `status: "ended"`

### 4. Полный switch

```java
return switch (dto.getType()) {
    case "make_call" -> makeCall(dto);
    case "accept_call" -> acceptCall(dto);
    case "reject_call" -> rejectCall(dto);
    case "leave_room" -> leaveRoom(dto);
    default -> throw badRequest("Unknown type: " + dto.getType());
};
```

### 5. Unit-тесты

**Файл:** `service/SignalingServiceCallActionsTest.java`

| Сценарий | Ожидание |
|----------|----------|
| make → accept | active |
| make → reject | нет в map |
| make → accept → leave | map пуст |
| accept без make | 404 |

## Критерии готовности

- [ ] Все 4 type реализованы
- [ ] Переход calling → active только через accept
- [ ] Тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingServiceCallActionsTest
```

## Связанные задачи

- [09-controller-rest.md](./09-controller-rest.md)
