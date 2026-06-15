# 06 — SignalingService: make_call

## Цель

Инициация звонка: UUID `roomId`, комната в статусе `calling`, ответ `SignalingResponseDto`.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [05-service-storage-validation.md](./05-service-storage-validation.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. Логика

При `type == "make_call"`:

1. `validateBusinessRules(dto)`
2. `roomId = UUID.randomUUID().toString()`
3. `CallRoom.calling(roomId, senderId, targetId)` → `rooms.put`
4. `return SignalingResponseDto.of(roomId, RoomStatus.CALLING)`

### 2. Конфликты

См. [07-service-conflicts.md](./07-service-conflicts.md): дубликат `calling` для пары teacher/student → **409**.

### 3. `handle()` — ветка make_call

```java
return switch (dto.getType()) {
    case "make_call" -> makeCall(dto);
    // остальные — позже
    default -> throw unsupported(dto.getType());
};
```

### 4. Unit-тесты

**Файл:** `service/SignalingServiceMakeCallTest.java`

| Кейс | Ожидание |
|------|----------|
| Валидный make_call | status calling, UUID roomId |
| Комната в map | findRoom present |
| teacherId / studentId | = sender / target |

## Критерии готовности

- [ ] UUID для каждой новой комнаты
- [ ] Ответ с `room_id` и `calling`
- [ ] Тесты зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingServiceMakeCallTest
```

## Связанные задачи

- [07-service-conflicts.md](./07-service-conflicts.md)
- [08-service-call-actions.md](./08-service-call-actions.md)
