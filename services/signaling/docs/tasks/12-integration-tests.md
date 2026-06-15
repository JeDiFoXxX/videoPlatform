# 12 — Сквозные интеграционные тесты

## Цель

Полный поток через Spring-контекст: REST → Service → Map → (опционально) cleanup.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [09-controller-rest.md](./09-controller-rest.md)
- [10-scheduler-cleanup.md](./10-scheduler-cleanup.md)
- [11-exception-handler.md](./11-exception-handler.md)

## Зависимости

Используется `spring-boot-starter-test` из [00](./00-bootstrap.md). Новых не добавлять.

## Шаги (Red → Green → Refactor)

### 1. Happy path

**Файл:** `SignalingFlowIntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class SignalingFlowIntegrationTest {

    @Autowired MockMvc mockMvc;

    @BeforeEach
    void clearRooms() { /* package-private clear в сервисе для тестов */ }
}
```

1. POST make_call → `calling`, сохранить `room_id`
2. POST accept_call (student) → `active`
3. POST leave_room → `ended`, повторный accept → 404

### 2. Reject path

1. make_call
2. reject_call
3. accept_call → 404

### 3. Scheduler + Clock

1. make_call
2. `Clock` +3 min → `removeExpiredRooms()`
3. accept → 404

Не ждать 2 реальные минуты в CI.

### 4. Изоляция

`@BeforeEach` очищает map — не `@DirtiesContext` на весь класс (быстрее).

### 5. CI

`mvn verify` в GitHub Actions должен включать эти тесты.

## Критерии готовности

- [ ] Минимум 2 E2E сценария
- [ ] Тест cleanup с mock time
- [ ] `mvn test` зелёный

## Команды проверки

```bash
.\mvnw.cmd test
.\mvnw.cmd verify
```

## Связанные задачи

- [13-websocket-auth.md](./13-websocket-auth.md)
- [14-makefile-ci.md](./14-makefile-ci.md)
