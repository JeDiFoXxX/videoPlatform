# 10 — RoomCleanupScheduler и @EnableScheduling

## Цель

Фоновая очистка комнат:


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

- `calling` старше **2 минут**
- `active` старше **180 минут**

Интервал: `@Scheduled(fixedRate = 60000)`.

**Здесь же** впервые включается планирование Spring — `@EnableScheduling` на главном классе или отдельном `@Configuration`.

## Предусловия

- [00-bootstrap.md](./00-bootstrap.md) — **без** `@EnableScheduling`
- [04-model-call-room.md](./04-model-call-room.md) — `createdAt`
- [08-service-call-actions.md](./08-service-call-actions.md) — комнаты в map

## Зависимости

Новых Maven-зависимостей **не** нужно — scheduling в Spring Context (webmvc).

## Шаги (Red → Green → Refactor)

### 1. Включить scheduling

**Вариант A (рекомендуется):** отдельная конфигурация

**Файл:** `config/SchedulingConfig.java`

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
```

**Вариант B:** на `SignalingApplication` — только если не хотите отдельный класс.

**Не** добавлять `@EnableScheduling` в [00-bootstrap.md](./00-bootstrap.md).

### 2. `RoomCleanupScheduler`

**Файл:** `service/RoomCleanupScheduler.java`

```java
@Component
public class RoomCleanupScheduler {

    private static final Duration CALLING_TTL = Duration.ofMinutes(2);
    private static final Duration ACTIVE_TTL = Duration.ofMinutes(180);

    private final SignalingService signalingService;
    private final Clock clock;

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredRooms() {
        signalingService.removeExpiredRooms(clock.instant());
    }
}
```

### 3. Метод в `SignalingService`

```java
public void removeExpiredRooms(Instant now) {
    rooms.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
}
```

TTL от `createdAt` (не от момента accept) — как в ТЗ v1.

### 4. `Clock` bean

**Файл:** `config/ClockConfig.java`

```java
@Bean
Clock clock() {
    return Clock.systemDefaultZone();
}
```

### 5. Unit-тесты

**Файл:** `service/RoomCleanupSchedulerTest.java`

| status | age | Результат |
|--------|-----|-----------|
| CALLING | 3 min | удалена |
| CALLING | 1 min | остаётся |
| ACTIVE | 181 min | удалена |
| ACTIVE | 10 min | остаётся |

Вызов `removeExpiredRooms` напрямую с `Clock.fixed` — **без** ожидания 60 с в CI.

### 6. Тест включения scheduling

**Файл:** `SchedulingEnabledTest.java` или метод в integration suite

```java
@SpringBootTest
class SchedulingEnabledTest {
    @Autowired ApplicationContext ctx;

    @Test
    void schedulingBeanPresent() {
        assertFalse(ctx.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class).isEmpty());
    }
}
```

## Критерии готовности

- [ ] `@EnableScheduling` только в config этой задачи (не в 00)
- [ ] `@Scheduled(fixedRate = 60000)`
- [ ] Пороги 2 и 180 минут
- [ ] Тесты с `Clock` зелёные

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=RoomCleanupSchedulerTest,SchedulingEnabledTest
```

## Dev-подсказка

Profile `application-dev.properties`: укороченные TTL только для ручной отладки.

## Связанные задачи

- [12-integration-tests.md](./12-integration-tests.md)
