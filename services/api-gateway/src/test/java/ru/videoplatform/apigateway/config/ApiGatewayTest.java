package ru.videoplatform.apigateway.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import ru.videoplatform.apigateway.config.filter.TelegramAuthFilter;
import ru.videoplatform.apigateway.exception.GatewayGlobalExceptionHandler;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "gateway.rate-limit.capacity=5",
                "gateway.rate-limit.refill-per-minute=1",
                "telegram.secret-token=test_secret_bot_token",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "services.keycloak.client-id=test-gateway-client-id",
                "services.keycloak.client-secret=test-gateway-secret",
                "services.keycloak.base-uri=http://localhost:4444",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri="
                        + "http://localhost:4444/realms/videoplatform"
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
public class ApiGatewayTest {

    @MockitoSpyBean
    private GatewayGlobalExceptionHandler globalExceptionHandler;

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

    @Autowired
    private TelegramAuthFilter telegramAuthFilter;

    @Test
    @DisplayName("Должен блокировать актуатор для неавторизованного запроса")
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
    @DisplayName("Должен блокировать актуатор для авторизованного пользователя")
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
    @DisplayName("Должен возвращать 401 Unauthorized для защищенных роутов без JWT")
    void shouldReturn401WhenAccessingProtectedRoutesAnonymously() {
        webTestClient.post()
                .uri("/bookings/test")
                .exchange()
                .expectStatus().isUnauthorized();
        webTestClient.get()
                .uri("/ws/test")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен пропускать разрешенные HTTP методы для авторизованного пользователя")
    void shouldAllowAccessToProtectedRoutesWhenAuthenticated() {
        bookingService.stubFor(post("/bookings/test")
                .willReturn(aResponse().withStatus(200)));
        bookingService.stubFor(get("/bookings/test").
                willReturn(aResponse().withStatus(200)));
        signalingService.stubFor(get("/ws/test")
                .willReturn(aResponse().withStatus(200)));

        webTestClient.post()
                .uri("/bookings/test")
                .exchange()
                .expectStatus().isOk();
        webTestClient.get()
                .uri("/bookings/test")
                .exchange()
                .expectStatus().isOk();
        webTestClient.get()
                .uri("/ws/test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен отклонять запрещенные HTTP методы")
    void shouldDenyForbiddenHttpMethodsEvenWhenAuthenticated() {
        clearRedisRateLimiterKeys();
        webTestClient.delete()
                .uri("/bookings/test")
                .exchange()
                .expectStatus().isNotFound();
        webTestClient.get()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .exchange()
                .expectStatus().isOk();
        webTestClient.post()
                .uri("/ws/test")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    @DisplayName("Должен блокировать любе неописанные в конфигурации маршруты")
    void shouldDenyAnyOtherUndefinedRequests() {
        webTestClient.get()
                .uri("/random/path")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Должен пропускать запрос при валидном секретном токен-заголовке Telegram")
    void shouldAllowRequestWhenTelegramSecretTokenIsValid() {
        clearRedisRateLimiterKeys();
        botService.stubFor(post("/test/event").willReturn(aResponse().withStatus(200)));
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("text"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Должен блокировать по Rate Limiter (429) с записью верного ключа в Redis")
    void shouldBlockWithRateLimitAndCreateCorrectRedisKey() {
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakUserStub();
        botService.stubFor(post("/test/event").willReturn(aResponse().withStatus(200)));
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("text"))
                .exchange()
                .returnResult(String.class)
                .getStatus();
        var responseStatus = webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("text"))
                .exchange()
                .returnResult(String.class)
                .getStatus();

        var keyOptional = redisTemplate.keys("request_rate_limiter.*").stream().findFirst();

        assertEquals(HttpStatus.OK, responseStatus);
        assertTrue(keyOptional.isPresent());
        assertTrue(keyOptional.get().contains("111111"));
        Mockito.verify(globalExceptionHandler, Mockito.times(1))
                .handle(
                        Mockito.any(ServerWebExchange.class),
                        Mockito.any(ResponseStatusException.class)
                );
    }

    @Test
    @DisplayName("Должен блокировать и мгновенно возвращать 200 OK при отсутствии ID в вебхуке")
    void shouldDropWith200AndAbortChainWhenTgIdIsMissing() {
        clearRedisRateLimiterKeys();
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createInvalidTelegramJsonBody())
                .exchange()
                .expectStatus().isOk();

        assertTrue(redisTemplate.keys("request_rate_limiter.*").isEmpty());
        verifyKeycloakTokenRequests(0);
    }

    @Test
    @DisplayName("Должен успешно парсить Telegram ID")
    void shouldSuccessfullyParseTelegramId() {
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakUserStub();
        botService.stubFor(post("/test/event").willReturn(aResponse().withStatus(200)));

        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token", "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("text"))
                .exchange()
                .expectStatus().isOk();

        verifyKeycloakTokenRequests(1);
        Mockito.verify(globalExceptionHandler, Mockito.never())
                .handle(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Должен перехватывать Exception при неуспешном парсинге Telegram ID")
    void shouldHandleExceptionWhenParsingTelegramIdFails() {
        clearRedisRateLimiterKeys();
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createInvalidTelegramJsonBody())
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(globalExceptionHandler, Mockito.times(1))
                .handle(
                        Mockito.any(ServerWebExchange.class),
                        Mockito.any(Exception.class)
                );
    }

    @Test
    @DisplayName("Должен перехватывать ошибку, если пользователя нет в Keycloak")
    void shouldDropWith200AndAbortChainWhenUserNotFoundInKeycloak() {
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakUserEmptyStub();
        webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("text"))
                .exchange()
                .expectStatus().isOk();

        verifyKeycloakTokenRequests(1);
        verifyKeycloakUserRequests();
        Mockito.verify(globalExceptionHandler, Mockito.times(1))
                .handle(
                        Mockito.any(ServerWebExchange.class),
                        Mockito.any(RuntimeException.class)
                );
    }

    @Test
    @DisplayName("Должен успешно регистрировать пользователя по ключу из Redis и обновить Keycloak")
    void shouldSuccessfullyRegisterUserWithCustomUuid() {
        var uuid = UUID.randomUUID().toString();
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakRegistrationStubs(uuid);
        redisTemplate.opsForValue().set("test_key", uuid);
        botService.stubFor(post("/test/event").willReturn(aResponse().withStatus(200)));
        var result = webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("/start test_key"))
                .exchange()
                .returnResult(String.class);

        assertEquals(HttpStatus.OK, result.getStatus());
        assertFalse(redisTemplate.hasKey("test_key"));
        verifyKeycloakRegistrationRequests(uuid);
    }

    @Test
    @DisplayName("Должен успешно пропускать пользователя, если /start без ключа, но ID есть в Keycloak")
    void shouldSuccessfullyPassUserWhenCleanStartReceivedAndUserExistsInKeycloak() {
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakUserStub();
        botService.stubFor(post("/test/event").willReturn(aResponse().withStatus(200)));
        var result = webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("/start"))
                .exchange()
                .returnResult(String.class);

        assertEquals(HttpStatus.OK, result.getStatus());
        verifyKeycloakUserRequests();
    }

    @Test
    @DisplayName("Должен выдать ошибку и 200 OK, если /start без ключа, но ID нет в Keycloak")
    void shouldReturnTelegramErrorWhenCleanStartReceivedAndUserDoesNotExistInKeycloak() {
        clearRedisRateLimiterKeys();
        setupKeycloakTokenStub();
        setupKeycloakUserEmptyStub();
        var responseStatus = webTestClient.post()
                .uri("/test/event")
                .header("X-Telegram-Bot-Api-Secret-Token",
                        "test_secret_bot_token")
                .bodyValue(createValidTelegramJsonBody("/start"))
                .exchange()
                .returnResult(String.class)
                .getStatus();

        verifyKeycloakUserRequests();
        assertEquals(HttpStatus.OK, responseStatus);
        Mockito.verify(globalExceptionHandler, Mockito.times(1))
                .handle(
                        Mockito.any(ServerWebExchange.class),
                        Mockito.any(RuntimeException.class)
                );
    }

    private void clearRedisRateLimiterKeys() {
        var existingKeys = redisTemplate.keys("request_rate_limiter.*");
        if (existingKeys != null && !existingKeys.isEmpty()) {
            redisTemplate.delete(existingKeys);
        }
    }

    private void setupKeycloakTokenStub() {
        keycloakService.stubFor(post(urlEqualTo(
                "/realms/videoplatform/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("{\"access_token\":\"test-system-token-123\",\"expires_in\":300}")));
    }

    private void setupKeycloakUserStub() {
        keycloakService.stubFor(get(urlPathEqualTo("/admin/realms/videoplatform/users"))
                .withQueryParam("q", equalTo("telegramId:111111"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("""
                                [
                                  {
                                    "id": "test-UUID",
                                    "firstName": "testFirstName",
                                    "lastName": "testLastName"
                                  }
                                ]
                                """)));
    }

    private void setupKeycloakUserEmptyStub() {
        keycloakService.stubFor(get(urlPathEqualTo("/admin/realms/videoplatform/users"))
                .withQueryParam("q", equalTo("telegramId:111111"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("[]")));
    }

    private void setupKeycloakRegistrationStubs(String uuid) {
        var keycloakUserJson = """
                {
                  "id": "%s",
                  "username": "testUserName",
                  "email": "test@test.com",
                  "enabled": true,
                  "firstName": "testFirstName",
                  "lastName": "testLastName",
                  "attributes": {
                    "telegramId": []
                  }
                }
                """.formatted(uuid);

        keycloakService.stubFor(get(urlEqualTo("/admin/realms/videoplatform/users/" + uuid))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody(keycloakUserJson)));

        keycloakService.stubFor(put(urlEqualTo("/admin/realms/videoplatform/users/" + uuid))
                .willReturn(aResponse()
                        .withStatus(204)));
    }

    private void verifyKeycloakRegistrationRequests(String uuid) {
        keycloakService.verify(1, getRequestedFor(urlEqualTo(
                "/admin/realms/videoplatform/users/" + uuid)));

        keycloakService.verify(1, putRequestedFor(urlEqualTo(
                "/admin/realms/videoplatform/users/" + uuid)));
    }

    private void verifyKeycloakTokenRequests(int times) {
        keycloakService.verify(times, postRequestedFor(urlEqualTo(
                "/realms/videoplatform/protocol/openid-connect/token")));
    }

    private void verifyKeycloakUserRequests() {
        keycloakService.verify(1, getRequestedFor(urlEqualTo(
                "/admin/realms/videoplatform/users?q=telegramId:111111")));
    }

    private String createValidTelegramJsonBody(String text) {
        return """
                {
                  "update_id": 123456,
                  "message": {
                    "message_id": 111111,
                    "from": {
                      "id": 111111,
                      "is_bot": false,
                      "first_name": "test_name"
                    },
                    "text": "%s"
                  }
                }
                """.formatted(text);
    }

    private String createInvalidTelegramJsonBody() {
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