package ru.videoplatform.apigateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "gateway.rate-limit.capacity=1",
                "gateway.rate-limit.refill-per-minute=1",
                "telegram.webhook.secret-token=test_secret_bot_token",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "telegram.webhook.tg-id-pattern=\"from\"\\\\s*:\\\\s*\\\\{\\\\s*\"id\"\\\\s*:\\\\s*(\\\\d+)",
                "services.keycloak.client-id=test-gateway-client-id",
                "services.keycloak.client-secret=test-gateway-secret",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:4444/realms/videoplatform"
        })
@AutoConfigureWebTestClient
@Import(TestConfig.class)
@EnableWireMock({
        @ConfigureWireMock(name = "bot-service", port = 1111,
                baseUrlProperties = {"services.bot-service.uri"}),
        @ConfigureWireMock(name = "signaling-service", port = 2222,
                baseUrlProperties = {"services.signaling-service.uri"}),
        @ConfigureWireMock(name = "booking-service", port = 3333,
                baseUrlProperties = {"services.booking-service.uri"}),
        @ConfigureWireMock(name = "keycloak-service", port = 4444)
})
public class ApiGatewayITest {

    @InjectWireMock("bot-service")
    private WireMockServer botService;

    @InjectWireMock("signaling-service")
    private WireMockServer signalingService;

    @InjectWireMock("booking-service")
    private WireMockServer bookingService;

    @InjectWireMock("keycloak-service")
    private WireMockServer keycloakService;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("Должен запрещать доступ к актуатору для неавторизованного запроса")
    void shouldDenyAccessToActuatorWhenAnonymous() {
        webTestClient.post()
                .uri("/actuator/test")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.get()
                .uri("/actuator/test")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен запрещать доступ к актуатору для авторизованного пользователя")
    void shouldDenyAccessToActuatorEvenWhenAuthenticated() {
        webTestClient.post()
                .uri("/actuator/test")
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.get()
                .uri("/actuator/test")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Должен возвращать 401 Unauthorized без JWT токена")
    void shouldReturn401WhenAccessingProtectedRoutesAnonymously() {
        webTestClient.post()
                .uri("/api/v1/booking/test")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.get()
                .uri("/ws/v1/test")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен пропускать разрешенные HTTP методы для авторизованного пользователя")
    void shouldAllowAccessToProtectedRoutesWhenAuthenticated() {
        bookingService.stubFor(post("/api/v1/booking/test")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        bookingService.stubFor(get("/api/v1/booking/test")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        signalingService.stubFor(get("/ws/v1/test")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        webTestClient.post()
                .uri("/api/v1/booking/test")
                .exchange()
                .expectStatus().isOk();
        webTestClient.get()
                .uri("/api/v1/booking/test")
                .exchange()
                .expectStatus().isOk();
        webTestClient.get()
                .uri("/ws/v1/test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен отклонять запрещенные HTTP методы для booking, bot и signaling сервисов")
    void shouldDenyForbiddenHttpMethodsEvenWhenAuthenticated() {
        clearRedisRateLimiterKeys();
        webTestClient.delete()
                .uri("/api/v1/booking/test")
                .exchange()
                .expectStatus().isNotFound();
        webTestClient.get()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .exchange()
                .expectStatus().isNotFound();
        webTestClient.post()
                .uri("/ws/v1/test")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен блокировать любые неописанные в конфигурации маршруты")
    void shouldDenyAnyOtherUndefinedRequests() {
        webTestClient.get()
                .uri("/random/path")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Должен успешно пропускать запрос при валидном секретном токене Telegram")
    void shouldAllowRequestWhenTelegramSecretTokenIsValid() {
        clearRedisRateLimiterKeys();
        botService.stubFor(post("/test/event")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createTelegramJsonBody())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Должен создавать ключ в Redis и блокировать запросы с кодом 429 при превышении лимита")
    void shouldBlockWithRateLimitAndCreateCorrectRedisKey() {
        clearRedisRateLimiterKeys();
        botService.stubFor(post("/test/event")
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        keycloakService.stubFor(post(urlEqualTo("/realms/videoplatform/protocol/openid-connect/token"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(containing("client_id=test-gateway-client"))
                .withRequestBody(containing("client_secret=test-gateway-secret"))
                .withRequestBody(containing("requested_subject=111111"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"fake_test_jwt_token_here\"}")));
        HttpStatusCode responseStatus = HttpStatus.ACCEPTED;
        for (int i = 0; i < 5; i++) {
            responseStatus = webTestClient.post()
                    .uri("/test/event")
                    .header("X-Telegram-Bot-Api-Secret-Token",
                            "test_secret_bot_token")
                    .bodyValue(createTelegramJsonBody())
                    .exchange()
                    .returnResult(String.class)
                    .getStatus();
            if (responseStatus.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                break;
            }
        }
        var keyOptional = redisTemplate.keys("request_rate_limiter.*").stream().findFirst();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, responseStatus);
        assertTrue(keyOptional.isPresent());
        assertTrue(keyOptional.get().contains("111111"));
    }

    @Test
    @DisplayName("Должен мгновенно возвращать 200 OK и блокировать, если в теле вебхука нет Telegram ID")
    void shouldDropWith200AndAbortChainWhenTgIdIsMissing() {
        clearRedisRateLimiterKeys();
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token", "test_secret_bot_token")
                .bodyValue(createTelegramJsonBodyWithoutId())
                .exchange()
                .expectStatus().isOk();
        assertTrue(redisTemplate.keys("request_rate_limiter.*").isEmpty());
        keycloakService.verify(0, postRequestedFor(urlEqualTo(
                "/realms/videoplatform/protocol/openid-connect/token")));
    }

    @Test
    @DisplayName("Должен возвращать 200 OK и блокировать, если tgId нет в базе Keycloak")
    void shouldDropWith200AndAbortChainWhenUserNotFoundInKeycloak() {
        clearRedisRateLimiterKeys();
        keycloakService.stubFor(post(urlEqualTo("/realms/videoplatform/protocol/openid-connect/token"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(containing("client_id=test-gateway-client-id"))
                .withRequestBody(containing("client_secret=test-gateway-secret"))
                .withRequestBody(containing("requested_subject=111111"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.UNAUTHORIZED.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\", \"error_description\": \"User not found\"}")));
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token", "test_secret_bot_token")
                .bodyValue(createTelegramJsonBody())
                .exchange()
                .expectStatus().isOk();
        var redisKeys = redisTemplate.keys("request_rate_limiter.*");
        assertTrue(redisKeys.isEmpty());
    }

    private void clearRedisRateLimiterKeys() {
        var existingKeys = redisTemplate.keys("request_rate_limiter.*");
        if (existingKeys != null && !existingKeys.isEmpty()) {
            redisTemplate.delete(existingKeys);
        }
    }

    private String createTelegramJsonBody() {
        return """
                {
                  "update_id": 123456,
                  "message": {
                    "message_id": 1,
                    "from": {
                      "id": 111111,
                      "is_bot": false,
                      "first_name": "test_name"
                    },
                    "text": "test"
                  }
                }
                """;
    }

    private String createTelegramJsonBodyWithoutId() {
        return """
                {
                  "update_id": 999999,
                  "message": {
                    "message_id": 2,
                    "text": "test"
                  }
                }
                """;
    }
}