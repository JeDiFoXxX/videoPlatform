package ru.videoplatform.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
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
import ru.videoplatform.apigateway.config.filter.TelegramAuthFilter;
import ru.videoplatform.apigateway.config.filter.TelegramIdParserFilter;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TelegramIdParserFilter telegramIdParserFilter;

    private final TelegramAuthFilter telegramAuthFilter;

    @Value("${telegram.webhook.secret-token}")
    private String telegramWebhookToken;

    @Value("${gateway.rate-limit.capacity}")
    private int rateLimitCapacity;

    @Value("${gateway.rate-limit.refill-per-minute}")
    private int rateLimitRefill;

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
                            return Mono.just(new AuthorizationDecision(
                                    telegramWebhookToken.equals(incomingSecret))
                            );
                        })
                        .pathMatchers("/api/**", "/ws/**").authenticated()
                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           RedisRateLimiter customRateLimiter,
                                           KeyResolver smartKeyResolver) {
        return builder.routes()
                .route("bot-service", route -> route
                        .path("/**/event")
                        .and().method("POST")
                        .filters(filter -> filter
                                .cacheRequestBody(String.class)
                                .filter(telegramIdParserFilter.apply())
                                .filter(telegramAuthFilter.apply())
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(customRateLimiter)
                                        .setKeyResolver(smartKeyResolver)))
                        .uri(botServiceUri))
                .route("booking-service", route -> route
                        .path("/api/v1/booking/**")
                        .and().method("GET", "POST")
                        .uri(bookingServiceUri))
                .route("signaling-service", route -> route
                        .path("/ws/v1/**")
                        .and().method("GET")
                        .uri(signalingServiceUri))
                .build();
    }

    @Bean
    public KeyResolver smartKeyResolver() {
        return exchange -> {
            if (exchange.getAttribute("extractedTgId") instanceof String telegramId) {
                return Mono.just(telegramId);
            }
            return Mono.empty();
        };
    }

    @Bean
    public RedisRateLimiter customRateLimiter() {
        int replenishRatePerSecond = Math.max(1, rateLimitRefill / 60);
        return new RedisRateLimiter(replenishRatePerSecond, rateLimitCapacity);
    }
}
