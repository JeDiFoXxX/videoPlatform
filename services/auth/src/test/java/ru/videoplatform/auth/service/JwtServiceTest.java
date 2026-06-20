package ru.videoplatform.auth.service;

import io.jsonwebtoken.JwtException;
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
    @DisplayName("Успешная генерация и парсинг токена с совпадением всех клеймов")
    void shouldGenerateAndParseValidTokenCorrectly() {
        var token = jwtService.generateAccessToken(user);
        var decodedToken = jwtService.parseToken(token);
        assertThat(decodedToken.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decodedToken.get("login", String.class)).isEqualTo(user.getLogin());
        assertThat(decodedToken.get("role", String.class)).isEqualTo(user.getRole().toString());
    }

    @Test
    @DisplayName("Выброс исключения при попытке распарсить токен со сломанной подписью")
    void shouldThrowExceptionWhenTokenSignatureIsInvalid() {
        var token = jwtService.generateAccessToken(user);
        var middle = token.length() / 2;
        var badToken = token.substring(0, middle) + "@" + token.substring(middle + 1);
        assertThatThrownBy(() -> jwtService.parseToken(badToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Успешное извлечение уникального идентификатора токена (jti)")
    void shouldExtractJtiFromValidToken() {
        var token = jwtService.generateAccessToken(user);
        var tokenJti = jwtService.parseToken(token).getId();
        assertThat(jwtService.extractJti(token)).isEqualTo(tokenJti);
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