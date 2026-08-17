package ru.videoplatform.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${telegram.webhook.secret-token}")
    private String telegramWebhookToken;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(registry -> registry
                        .pathMatchers("/actuator/**").denyAll()
                        .pathMatchers("/tg-webhook/**").access((authentication, context) -> {
                            var incomingSecret = context.getExchange()
                                    .getRequest()
                                    .getHeaders()
                                    .getFirst("X-Telegram-Bot-Api-Secret-Token");
                            return Mono.just(new AuthorizationDecision(telegramWebhookToken
                                    .equals(incomingSecret)));
                        })
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
