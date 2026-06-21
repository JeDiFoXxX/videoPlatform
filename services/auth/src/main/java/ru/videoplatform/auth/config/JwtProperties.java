package ru.videoplatform.auth.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
public class JwtProperties {
    private final String secret = "my-super-secret-key-32-symbols-minimum-length";
    private final Duration accessTokenLifetime = Duration.ofSeconds(900);
    private final Duration refreshTokenLifetime = Duration.ofDays(30);
}
