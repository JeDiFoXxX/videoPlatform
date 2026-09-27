package ru.videoplatform.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {

    @Value("${gateway.rate-limit.capacity}")
    private int rateLimitCapacity;

    @Value("${gateway.rate-limit.refill-per-minute}")
    private int rateLimitRefill;

    @Bean
    public KeyResolver smartKeyResolver() {
        return exchange -> {
            if (exchange.getAttribute("extractedTgId") instanceof String telegramId) {
                return Mono.just(telegramId);
            }
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        };
    }

    @Bean
    public RedisRateLimiter customRateLimiter() {
        return new RedisRateLimiter(rateLimitRefill, rateLimitCapacity, 5);
    }
}
