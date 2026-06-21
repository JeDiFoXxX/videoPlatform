package ru.videoplatform.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "my-super-secret-key-32-symbols-minimum",
                900);
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
        var jwtServiceTest = new JwtService(
                "my-super-secret-key-32-symbols-minimum",
                0);
        var token = jwtServiceTest.generateAccessToken(user);
        assertThat(jwtServiceTest.isTokenValid(token)).isFalse();
    }
}