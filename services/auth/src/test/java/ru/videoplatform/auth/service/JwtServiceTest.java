package ru.videoplatform.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.videoplatform.auth.config.JwtProperties;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Spy
    private JwtProperties jwtProperties = new JwtProperties();

    @InjectMocks
    private JwtService jwtService;

    @Test
    @DisplayName("Успешное извлечение уникального идентификатора токена (jti)")
    void shouldExtractJtiFromValidToken() {
        var token = jwtService.generateAccessToken(createUser());
        assertThat(jwtService.extractJti(token)).isNotNull();
    }

    @Test
    @DisplayName("Возврат null при попытке извлечь jti из поврежденного токена")
    void shouldReturnNullWhenExtractingJtiFromCorruptedToken() {
        var token = jwtService.generateAccessToken(createUser());
        var middle = token.length() / 2;
        var badToken = token.substring(0, middle) + "@" + token.substring(middle + 1);
        assertThat(jwtService.extractJti(badToken)).isNull();
    }

    @Test
    @DisplayName("Проверка валидности: рабочий токен должен возвращать true")
    void shouldReturnTrueWhenTokenIsValid() {
        var token = jwtService.generateAccessToken(createUser());
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Проверка валидности: просроченный токен должен возвращать false")
    void shouldReturnFalseWhenTokenIsExpired() {
        doReturn(Duration.ZERO).when(jwtProperties).getAccessTokenLifetime();
        var token = jwtService.generateAccessToken(createUser());
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .role(UserRole.ADMIN)
                .build();
    }
}