# 13 — WebSocket, auth и oauth2-resource-server (конечный этап)

## Цель

Реализовать транспорт из [docs/spec.md](../spec.md): `/ws/signaling?token=...`, сессии, маршрутизация signaling-сообщений, проверка JWT.


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

> **Post-v1.** REST из задач 00–12 должен быть зелёным.

## Предусловия

- [12-integration-tests.md](./12-integration-tests.md)
- Контракт auth-сервиса (validate JWT) или JWKS endpoint

## Зависимости (`pom.xml`) — конечный этап

### 1. OAuth2 Resource Server (если JWT валидируется локально)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Когда нужен:**

- auth отдаёт JWT, signaling проверяет подпись/JWKS сам;
- WebSocket handshake читает `token` query param → `JwtDecoder`.

**Когда НЕ нужен:**

- достаточно `RestClient` → `POST /auth/validate` — тогда oauth2 **не** добавлять.

Зафиксировать решение команды в README этой задачи.

### 2. WebSocket

`spring-boot-starter-websocket` — **уже в pom**.

### 3. Тесты WS

При необходимости оставить:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket-test</artifactId>
    <scope>test</scope>
</dependency>
```

если убрали в [00](./00-bootstrap.md) в пользу только `starter-test`.

## Шаги (Red → Green → Refactor)

### 1. Конфигурация WS

**Файлы:**

```
config/WebSocketConfig.java
handler/SignalingWebSocketHandler.java
session/SessionRegistry.java
```

- URL: `/ws/signaling?token=BASE64_JWT_TOKEN`
- On connect: валидация JWT → `userId` → registry

### 2. OAuth2 (если выбран resource server)

**`application.properties`:**

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${AUTH_ISSUER_URI}
# или jwk-set-uri
```

**Security filter chain:**

- HTTP: защита REST (если нужно) или только WS handshake
- WS: `HandshakeInterceptor` с извлечением JWT

### 3. Auth через RestClient (альтернатива без oauth2)

**Файл:** `client/AuthClient.java`

```java
public boolean validateToken(String token) {
    return restClient.post()
        .uri(authBaseUrl + "/validate")
        .body(new TokenRequest(token))
        .retrieve()
        .toBodilessEntity()
        .getStatusCode().is2xxSuccessful();
}
```

Без `spring-boot-starter-oauth2-resource-server`.

### 4. Типы сообщений (spec)

| type | Направление |
|------|-------------|
| `user_online` / `user_offline` | server → client |
| `make_call` / `incoming_call` | call flow |
| `accept_call` / `reject_call` | call flow |
| `sdp_offer` / `sdp_answer` / `ice_candidate` | WebRTC relay |
| `leave_room` | teardown |

Переиспользовать `SignalingService` для `make_call` / `accept_call` / …

### 5. Disconnect

On close → `user_offline`, удаление сессии, `leave_room` при активной комнате.

### 6. Тесты

- `@SpringBootTest` + WebSocket test client
- mock `AuthClient` или test JWT (resource server)
- connect → `user_online` broadcast (2 сессии)

## Критерии готовности

- [ ] WS connect с валидным токеном
- [ ] make_call → incoming_call у student
- [ ] sdp/ice ретрансляция в комнате
- [ ] disconnect → user_offline
- [ ] Зависимость oauth2 добавлена **только если** выбрана локальная JWT-валидация
- [ ] WS-тесты в CI

## Команды проверки

```bash
.\mvnw.cmd test -Dtest=SignalingWebSocketIntegrationTest
```

## Связанные задачи

- [14-makefile-ci.md](./14-makefile-ci.md) — actuator health перед деплоем
