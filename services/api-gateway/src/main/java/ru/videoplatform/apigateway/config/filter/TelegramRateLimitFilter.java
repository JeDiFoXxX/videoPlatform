package ru.videoplatform.apigateway.config.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TelegramRateLimitFilter {

    private final KeyResolver smartKeyResolver;
    private final RedisRateLimiter customRateLimiter;

    public GatewayFilter apply() {
        return (exchange, chain) -> smartKeyResolver.resolve(exchange)
                .flatMap(key -> customRateLimiter.isAllowed("bot-service", key))
                .flatMap(response -> {
                    if (!response.isAllowed()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS));
                    }
                    return chain.filter(exchange);
                });
    }
}
