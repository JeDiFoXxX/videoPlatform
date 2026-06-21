package ru.videoplatform.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.videoplatform.auth.config.JwtProperties;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Spy
    private JwtProperties jwtProperties = new JwtProperties();

    @InjectMocks
    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtProperties);
        user = User.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .role(UserRole.ADMIN)
                .build();
    }


    @Test
    @DisplayName("Успешное извлечение уникального идентификатора токена (jti)")
    void shouldExtractJtiFromValidToken() {
        var token = jwtService.generateAccessToken(user);
        assertThat(jwtService.extractJti(token)).isNotNull();
    }

    @Test
    @DisplayName("Возврат null при попытке извлечь jti из поврежденного токена")
    void shouldReturnNullWhenExtractingJtiFromCorruptedToken() {
        var token = jwtService.generateAccessToken(user);
        var middle = token.length() / 2;
        var badToken = token.substring(0, middle) + "@" + token.substring(middle + 1);
        assertThat(jwtService.extractJti(badToken)).isNull();
    }

    @Test
    @DisplayName("Проверка валидности: рабочий токен должен возвращать true")
    void shouldReturnTrueWhenTokenIsValid() {
        var token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Проверка валидности: просроченный токен (TTL=0) должен возвращать false")
    void shouldReturnFalseWhenTokenIsExpired() {
        JwtProperties expiredProperties = new JwtProperties();
        ReflectionTestUtils
                .setField(expiredProperties, "secret", "my-super-secret-key-32-symbols-minimum-length");
        ReflectionTestUtils
                .setField(expiredProperties, "accessTokenLifetime", Duration.ZERO);
        var jwtServiceTest = new JwtService(expiredProperties);
        var token = jwtServiceTest.generateAccessToken(user);

        assertThat(jwtServiceTest.isTokenValid(token)).isFalse();
    }
}