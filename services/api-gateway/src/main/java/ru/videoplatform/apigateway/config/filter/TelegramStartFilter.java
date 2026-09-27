package ru.videoplatform.apigateway.config.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TelegramStartFilter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Value("${services.keycloak.base-uri}")
    private String keycloakBaseUri;

    public GatewayFilter apply() {
        return (exchange, chain) -> {
            var jsonString = Objects.requireNonNull(
                    exchange.getAttribute("cachedRequestBody")).toString();

            if (!jsonString.contains("/start")) {
                return chain.filter(exchange);
            }

            var systemToken = Objects.requireNonNull(
                    exchange.getAttribute("keycloakSystemToken")).toString();
            var telegramId = Objects.requireNonNull(
                    exchange.getAttribute("extractedTgId")).toString();

            var redisKey = extractUserId(jsonString);

            return Mono.defer(() -> {
                        if (redisKey.isEmpty()) {
                            return checkUserFromKeycloak(telegramId, systemToken);
                        }

                        return getUserIdFromRedis(redisKey)
                                .flatMap(userId -> getUserFromKeycloak(userId, systemToken))
                                .flatMap(user -> saveTelegramIdToKeycloak(user, systemToken, telegramId))
                                .then(redisTemplate.opsForValue().delete(redisKey))
                                .then();
                    })
                    .then(Mono.defer(() -> chain.filter(exchange)));
        };
    }

    private Mono<String> getUserIdFromRedis(String redisKey) {
        return redisTemplate.opsForValue().get(redisKey)
                .switchIfEmpty(Mono.error(RuntimeException::new));
    }

    private Mono<KeycloakUserResponse> getUserFromKeycloak(String userId, String systemToken) {
        return webClient.get()
                .uri(keycloakBaseUri + "/admin/realms/videoplatform/users/" + userId)
                .headers(headers -> headers.setBearerAuth(systemToken))
                .retrieve()
                .bodyToMono(KeycloakUserResponse.class)
                .onErrorMap(error -> new RuntimeException());
    }

    private Mono<Void> checkUserFromKeycloak(String telegramId, String systemToken) {
        return webClient.get()
                .uri(keycloakBaseUri + "/admin/realms/videoplatform/users?q=telegramId:" + telegramId)
                .headers(headers -> headers.setBearerAuth(systemToken))
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .collectList()
                .filter(list -> list.size() == 1)
                .switchIfEmpty(Mono.error(RuntimeException::new))
                .then();
    }

    private Mono<Void> saveTelegramIdToKeycloak(KeycloakUserResponse user, String systemToken, String telegramId) {
        var userUri = keycloakBaseUri + "/admin/realms/videoplatform/users/" + user.id();
        var updatedAttributes = new TelegramAttributes(List.of(telegramId));

        var updatedUser = new KeycloakUserResponse(
                user.id(),
                user.username(),
                user.email(),
                user.enabled(),
                user.firstName(),
                user.lastName(),
                updatedAttributes
        );

        return webClient.put()
                .uri(userUri)
                .headers(headers -> headers.setBearerAuth(systemToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .retrieve()
                .toBodilessEntity()
                .onErrorMap(error -> new RuntimeException())
                .then();
    }

    @SneakyThrows
    private String extractUserId(String jsonString) {
        var update = objectMapper.readValue(jsonString, TelegramUpdate.class);
        var fullText = update.message().text();
        return fullText.length() <= 7 ? "" : fullText.substring(7);
    }

    private record TelegramUpdate(@JsonProperty("message") TelegramMessage message) {
    }

    private record TelegramMessage(@JsonProperty("text") String text) {
    }

    private record KeycloakUserResponse(
            String id,
            String username,
            String email,
            boolean enabled,
            String firstName,
            String lastName,
            @JsonProperty("attributes") TelegramAttributes attributes) {
    }

    private record TelegramAttributes(@JsonProperty("telegramId") List<String> telegramId) {
    }
}
