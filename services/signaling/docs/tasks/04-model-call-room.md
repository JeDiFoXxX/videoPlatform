# 04 — CallRoom (модель в памяти)

## Цель

Модель виртуальной комнаты для `ConcurrentHashMap`: участники, статус, `createdAt` для scheduler.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [01-room-status.md](./01-room-status.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. Класс `CallRoom`

**Файл:** `src/main/java/ru/videoplatform/signaling/model/CallRoom.java`

| Поле | Тип |
|------|-----|
| `roomId` | `String` |
| `teacherId` | `String` |
| `studentId` | `String` |
| `status` | `RoomStatus` |
| `createdAt` | `LocalDateTime` |

### 2. Factory

```java
public static CallRoom calling(String roomId, String teacherId, String studentId) {
    return new CallRoom(roomId, teacherId, studentId, RoomStatus.CALLING, LocalDateTime.now());
}
```

Роли v1: `make_call` → `teacherId = senderId`, `studentId = targetId`.

### 3. Immutable API

Метод `withStatus(RoomStatus status)` для перехода `CALLING` → `ACTIVE`.

### 4. Unit-тест

**Файл:** `src/test/java/ru/videoplatform/signaling/model/CallRoomTest.java`

- Все поля заданы после factory.
- `createdAt` не null.
- `withStatus(ACTIVE)` меняет только status.

## Критерии готовности

- [ ] Поля из ТЗ v1
- [ ] `createdAt` при создании
- [ ] `CallRoomTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=CallRoomTest
```

## Связанные задачи

- [05-service-storage-validation.md](./05-service-storage-validation.md)
- [10-scheduler-cleanup.md](./10-scheduler-cleanup.md)
