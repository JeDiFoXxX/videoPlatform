package ru.videoplatform.apigateway.config.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramUserEnrichmentFilter {

    private final WebClient webClient;

    @Value("${services.keycloak.base-uri}")
    private String keycloakBaseUri;

    public GatewayFilter apply() {
        return (exchange, chain) -> {
            var telegramId = Objects.requireNonNull(
                    exchange.getAttribute("extractedTgId")).toString();
            if (telegramId != null) {

                var systemToken = Objects.requireNonNull(
                        exchange.getAttribute("keycloakSystemToken")).toString();

                return getUserFromTelegramId(systemToken, telegramId)
                        .flatMap(response -> {
                            var mutatedExchange = mutateExchange(systemToken, exchange, response);
                            return chain.filter(mutatedExchange);
                        });
            }
            return Mono.error(new RuntimeException());
        };
    }

    private Mono<KeycloakUserResponse> getUserFromTelegramId(String systemToken, String telegramId) {
        return webClient.get()
                .uri(keycloakBaseUri + "/admin/realms/videoplatform/users?q=telegramId:" + telegramId)
                .headers(headers -> headers.setBearerAuth(systemToken))
                .retrieve()
                .bodyToFlux(KeycloakUserResponse.class)
                .collectList()
                .filter(list -> list.size() == 1)
                .map(List::getFirst)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new RuntimeException())));
    }

    private ServerWebExchange mutateExchange(String systemToken,
                                             ServerWebExchange exchange,
                                             KeycloakUserResponse response) {
        var modifiedRequest = exchange.getRequest().mutate()
                .header("Authorization", "Bearer " + systemToken)
                .header("Student-id", response.id() != null ? response.id() : "")
                .header("First-name", response.firstName() != null ? response.firstName() : "")
                .header("Last-name", response.lastName() != null ? response.lastName() : "")
                .build();

        return exchange.mutate()
                .request(modifiedRequest)
                .build();
    }

    public record KeycloakUserResponse(String id, String firstName, String lastName) {
    }
}
