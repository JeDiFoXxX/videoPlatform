package ru.videoplatform.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import ru.videoplatform.apigateway.config.filter.*;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TelegramIdParserFilter telegramIdParserFilter;
    private final TelegramAuthFilter telegramAuthFilter;
    private final TelegramStartFilter telegramStartFilter;
    private final TelegramUserEnrichmentFilter telegramUserEnrichmentFilter;
    private final TelegramRateLimitFilter telegramRateLimitFilter;

    @Value("${telegram.secret-token}")
    private String telegramToken;

    @Value("${services.signaling-service.uri}")
    private String signalingServiceUri;

    @Value("${services.booking-service.uri}")
    private String bookingServiceUri;

    @Value("${services.bot-service.uri}")
    private String botServiceUri;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(registry -> registry
                        .pathMatchers("/actuator/**").denyAll()
                        .pathMatchers("/**/event").access((authentication, context) -> {
                            var exchange = context.getExchange();
                            var incomingSecret = exchange.getRequest().getHeaders()
                                    .getFirst("X-Telegram-Bot-Api-Secret-Token");
                            boolean isValid = telegramToken.equals(incomingSecret);
                            return Mono.just(new AuthorizationDecision(isValid));
                        })
                        .pathMatchers("/bookings/**", "/ws/**").authenticated()
                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("bot-service", route -> route
                        .path("/**/event")
                        .and().method("POST")
                        .filters(filter -> filter
                                .cacheRequestBody(String.class)
                                .filter(telegramIdParserFilter.apply())
                                .filter(telegramRateLimitFilter.apply())
                                .filter(telegramAuthFilter.apply())
                                .filter(telegramStartFilter.apply())
                                .filter(telegramUserEnrichmentFilter.apply()))
                        .uri(botServiceUri))
                .route("booking-service", route -> route
                        .path("/bookings/**")
                        .and().method("GET", "POST")
                        .uri(bookingServiceUri))
                .route("signaling-service", route -> route
                        .path("/ws/**")
                        .and().method("GET")
                        .uri(signalingServiceUri))
                .build();
    }
}
