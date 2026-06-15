# Задачи разработки signaling

Пошаговый план v1 (REST + in-memory) и roadmap (WebSocket, auth, CI) для компонента **videoPlatform**.  
Спецификация: [docs/spec.md](../spec.md).

## Методология TDD

Каждая задача выполняется циклом **Red → Green → Refactor**:

1. **Red** — написать падающий тест (`mvn test` должен упасть).
2. **Green** — минимальная реализация.
3. **Refactor** — улучшить код, тесты остаются зелёными.

Порядок: **unit-тесты сервиса/модели → контроллер (MockMvc) → E2E** ([12](./12-integration-tests.md)).  
Тестовые хелперы и `Clock` — [15](./15-test-support.md) (выполнить после [05](./05-service-storage-validation.md), до [06](./06-service-make-call.md)).

Базовый пакет: `ru.videoplatform.signaling`.

`@EnableScheduling` — только в [10-scheduler-cleanup.md](./10-scheduler-cleanup.md).

## Зависимости Maven (по этапам)

| Зависимость | Задача | Назначение |
|-------------|--------|------------|
| *(уже в pom)* `spring-boot-starter-webmvc` | — | REST API |
| *(уже в pom)* `spring-boot-starter-websocket` | 13 | WebSocket (позже) |
| `spring-boot-starter-test` | [00](./00-bootstrap.md) | Единый test-starter |
| `spring-boot-starter-validation` | [05](./05-service-storage-validation.md) | `jakarta.validation` на DTO |
| `spring-boot-starter-actuator` | [14](./14-makefile-ci.md) | `/actuator/health` для Docker/CI |
| `spring-boot-starter-oauth2-resource-server` | [13](./13-websocket-auth.md) | JWT (позже) |

## Порядок v1

| # | Файл | Red (тест) | Green (код) |
|---|------|------------|-------------|
| 00 | [00-bootstrap.md](./00-bootstrap.md) | `SignalingApplicationTests` | Конфиг, test-starter |
| 01 | [01-room-status.md](./01-room-status.md) | `RoomStatusTest` | `RoomStatus` |
| 02 | [02-dto-signaling.md](./02-dto-signaling.md) | `SignalingDtoSerializationTest` | `SignalingDto` |
| 03 | [03-dto-response.md](./03-dto-response.md) | `SignalingResponseDtoSerializationTest` | Response DTO |
| 04 | [04-model-call-room.md](./04-model-call-room.md) | `CallRoomTest` | `CallRoom` |
| 05 | [05-service-storage-validation.md](./05-service-storage-validation.md) | `SignalingServiceValidationTest` | Map + validation |
| 15 | [15-test-support.md](./15-test-support.md) | `SignalingTestSupportTest` | Clock, фабрики DTO |
| 06 | [06-service-make-call.md](./06-service-make-call.md) | `SignalingServiceMakeCallTest` | `make_call` |
| 07 | [07-service-conflicts.md](./07-service-conflicts.md) | `SignalingServiceConflictsTest` | 404/409 |
| 08 | [08-service-call-actions.md](./08-service-call-actions.md) | `SignalingServiceActionsTest` | accept/reject/leave |
| 09 | [09-controller-rest.md](./09-controller-rest.md) | `SignalingControllerIntegrationTest` | REST controller |
| 10 | [10-scheduler-cleanup.md](./10-scheduler-cleanup.md) | `RoomCleanupSchedulerTest` | TTL cleanup |
| 11 | [11-exception-handler.md](./11-exception-handler.md) | `SignalingExceptionHandlerTest` | Error JSON |
| 12 | [12-integration-tests.md](./12-integration-tests.md) | `SignalingFlowIntegrationTest` | E2E |
| 13 | [13-websocket-auth.md](./13-websocket-auth.md) | `SignalingWebSocketTest` | WS + JWT |
| 14 | [14-makefile-ci.md](./14-makefile-ci.md) | `ActuatorHealthIntegrationTest` | Docker, CI |

## Статус

| Задача | Статус |
|--------|--------|
| 00–15 | не начато |
