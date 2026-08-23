package ru.videoplatform.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TgAuthFilter {

    @Autowired
    private WebClient webClient;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakUrl;

    @Value("${services.keycloak.client-id}")
    private String keycloakClientId;

    @Value("${services.keycloak.client-secret}")
    private String keycloakClientSecret;

    @Value("${telegram.webhook.tg-id-pattern}")
    private String tgIdPattern;

    public GatewayFilter apply() {
        var pattern = Pattern.compile(tgIdPattern);
        var tokenUri = keycloakUrl + "/protocol/openid-connect/token";
        return (exchange, chain) -> {
            var cachedBody = exchange.getAttribute("cachedRequestBody");
            if (cachedBody instanceof String jsonString) {
                var matcher = pattern.matcher(jsonString);
                if (matcher.find()) {
                    var tgUserId = matcher.group(1);
                    return webClient.post()
                            .uri(tokenUri)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(BodyInserters.fromFormData("grant_type",
                                            "urn:ietf:params:oauth:grant-type:token-exchange")
                                    .with("client_id", keycloakClientId)
                                    .with("client_secret", keycloakClientSecret)
                                    .with("requested_subject", tgUserId)
                                    .with("requested_token_type",
                                            "urn:ietf:params:oauth:token-type:access_token"))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(jsonMap -> (String) jsonMap.get("access_token"))
                            .flatMap(jwtToken -> {
                                var modifiedRequest = exchange.getRequest().mutate()
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                                        .build();
                                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                            })
                            .onErrorResume(e -> onTgInvalidRequestDropTo200(exchange));
                }
            }
            return onTgInvalidRequestDropTo200(exchange);
        };
    }

    private Mono<Void> onTgInvalidRequestDropTo200(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        return response.setComplete();
    }
}
