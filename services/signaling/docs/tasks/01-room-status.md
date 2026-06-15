# 01 — RoomStatus (статусы комнаты)

## Цель

Типобезопасные статусы комнаты. В JSON — строки `"calling"`, `"active"` (и при необходимости `"rejected"`, `"ended"`).


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [00-bootstrap.md](./00-bootstrap.md) выполнена.

## Зависимости

Новых зависимостей **не** добавлять. Jackson для enum — из `spring-boot-starter-webmvc` (`@JsonValue` / `@JsonCreator`).

## Шаги (Red → Green → Refactor)

### 1. Enum `RoomStatus`

**Файл:** `src/main/java/ru/videoplatform/signaling/model/RoomStatus.java`

| Constant | Wire value | Назначение |
|----------|------------|------------|
| `CALLING` | `calling` | Вызов инициирован, не принят |
| `ACTIVE` | `active` | Созвон принят |
| `REJECTED` | `rejected` | Отклонён |
| `ENDED` | `ended` | Завершён явно |

```java
public enum RoomStatus {
    CALLING("calling"),
    ACTIVE("active"),
    REJECTED("rejected"),
    ENDED("ended");

    private final String value;

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static RoomStatus fromValue(String value) { ... }
}
```

Использовать аннотации Jackson из стека Spring Boot 4 (`com.fasterxml.jackson.annotation.*` или `tools.jackson.annotation.*` — как компилирует проект; зафиксировать один пакет).

### 2. Связь с DTO

`SignalingDto.type` — это `make_call` / `accept_call`, **не** статус комнаты.  
`RoomStatus` — для `CallRoom` и `SignalingResponseDto.status`.

### 3. Unit-тест

**Файл:** `src/test/java/ru/videoplatform/signaling/model/RoomStatusTest.java`

| Кейс | Ожидание |
|------|----------|
| `getValue()` для `CALLING` | `"calling"` |
| `fromValue("active")` | `ACTIVE` |
| `fromValue("unknown")` | `IllegalArgumentException` |
| Сериализация через `ObjectMapper` (@SpringBootTest) | `"calling"` в JSON |
| Десериализация `"active"` | `ACTIVE` |

## Критерии готовности

- [ ] Enum в пакете `model`
- [ ] `calling` / `active` совпадают с TTL в [10](./10-scheduler-cleanup.md)
- [ ] `RoomStatusTest` зелёный

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=RoomStatusTest
```

## Связанные задачи

- [04-model-call-room.md](./04-model-call-room.md)
- [06-service-make-call.md](./06-service-make-call.md)
