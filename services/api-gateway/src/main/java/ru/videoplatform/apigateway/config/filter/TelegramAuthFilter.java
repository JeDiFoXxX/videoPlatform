package ru.videoplatform.apigateway.config.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramAuthFilter {

    private final WebClient webClient;

    @Value("${services.keycloak.base-uri}")
    private String keycloakBaseUri;

    @Value("${services.keycloak.client-id}")
    private String keycloakClientId;

    @Value("${services.keycloak.client-secret}")
    private String keycloakClientSecret;

    public GatewayFilter apply() {
        return (exchange, chain) -> {
            if (exchange.getAttribute("extractedTgId") instanceof String telegramId) {
                return getSystemToken()
                        .flatMap(systemToken -> getUserFromTelegramId(systemToken, telegramId)
                                .flatMap(response ->
                                        chain.filter(mutateExchange(systemToken, exchange, response)))
                        );
            }
            return Mono.error(new RuntimeException());
        };
    }

    private Mono<String> getSystemToken() {
        return webClient.post()
                .uri(keycloakBaseUri + "/realms/videoplatform/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", keycloakClientId)
                        .with("client_secret", keycloakClientSecret))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .map(KeycloakTokenResponse::accessToken);
    }

    private Mono<KeycloakUserResponse> getUserFromTelegramId(String clientToken, String telegramId) {
        return webClient.get()
                .uri(keycloakBaseUri + "/admin/realms/videoplatform/users?q=telegramId:" + telegramId)
                .headers(headers -> headers.setBearerAuth(clientToken))
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .collectList()
                .filter(list -> list.size() == 1)
                .map(List::getFirst)
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new RuntimeException("User not found or not unique")))
                );
    }

    private ServerWebExchange mutateExchange(String systemToken,
                                             ServerWebExchange exchange,
                                             KeycloakUserResponse response) {
        var modifiedRequest = exchange.getRequest().mutate()
                .header("Authorization", "Bearer " + systemToken)
                .header("User-id", response.id() != null ? response.id() : "")
                .header("First-name", response.firstName() != null ? response.firstName() : "")
                .header("Last-name", response.lastName() != null ? response.lastName() : "")
                .build();
        return exchange.mutate()
                .request(modifiedRequest)
                .build();
    }

    private record KeycloakTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Integer expiresIn
    ) { }

    public record KeycloakUserResponse(String id, String firstName, String lastName) { }
}
