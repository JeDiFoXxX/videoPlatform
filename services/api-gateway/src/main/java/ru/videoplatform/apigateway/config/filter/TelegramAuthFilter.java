package ru.videoplatform.apigateway.config.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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
        return (exchange, chain) -> webClient.post()
                .uri(keycloakBaseUri + "/realms/videoplatform/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", keycloakClientId)
                        .with("client_secret", keycloakClientSecret))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .map(KeycloakTokenResponse::accessToken)
                .onErrorMap(RuntimeException::new)
                .flatMap(systemToken -> {
                    exchange.getAttributes().put("keycloakSystemToken", systemToken);
                    return chain.filter(exchange);
                });
    }

    private record KeycloakTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Integer expiresIn
    ) { }
}
