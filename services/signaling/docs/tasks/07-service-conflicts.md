# 07 — SignalingService: конфликты и инварианты

## Цель

Единые правила 400/404/409 и проверка участников комнаты.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [06-service-make-call.md](./06-service-make-call.md)

## Зависимости

Без новых.

## Шаги (Red → Green → Refactor)

### 1. Таблица ошибок v1

| Ситуация | HTTP |
|----------|------|
| Bean Validation / бизнес-валидация | 400 |
| Комната не найдена | 404 |
| Дубликат calling для пары | 409 |
| accept не от student | 403 или 400 |
| accept при status != calling | 409 |
| make_call при занятом student | 409 |

### 2. Поиск дубликата

```java
Optional<CallRoom> findCallingRoom(String teacherId, String studentId)
```

Линейный поиск по `rooms.values()` достаточен для v1.

### 3. Проверка участника

```java
void assertStudent(CallRoom room, String senderId)
void assertParticipant(CallRoom room, String senderId)
```

### 4. Unit-тесты

**Файл:** `service/SignalingServiceConflictsTest.java`

- Второй make_call той же пары → 409
- accept с неверным room_id → 404
- accept от teacher → 403/400

## Критерии готовности

- [ ] Поведение зафиксировано тестами
- [ ] Исключения готовы к [11-exception-handler.md](./11-exception-handler.md)

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingServiceConflictsTest
```

## Связанные задачи

- [08-service-call-actions.md](./08-service-call-actions.md)
- [11-exception-handler.md](./11-exception-handler.md)
