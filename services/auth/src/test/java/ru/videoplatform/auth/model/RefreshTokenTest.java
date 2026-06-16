package ru.videoplatform.auth.model;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Должен успешно сохранять и загружать RefreshToken со связью User")
    void shouldSaveAndLoadRefreshToken() {
        var user = User.builder()
                .login("teacherLogin")
                .passwordHash("hashPassword")
                .role(UserRole.TEACHER)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();
        var savedUser = entityManager.find(User.class, user.getId());
        var token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(savedUser)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        entityManager.persist(token);
        entityManager.flush();
        entityManager.clear();
        var savedToken = entityManager.find(RefreshToken.class, token.getId());
        assertThat(savedToken.getId()).isNotNull();
        assertThat(token.getToken()).isEqualTo(savedToken.getToken());
        assertThat(savedUser.getId()).isEqualTo(savedToken.getUser().getId());
    }

    @Test
    @DisplayName("Должен выбрасывать ConstraintViolationException для дубликата")
    void shouldThrowExceptionWhenTokenIsNotUnique() {
        var user = User.builder()
                .login("teacherLogin")
                .passwordHash("hashPassword")
                .role(UserRole.TEACHER)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();
        var savedUser = entityManager.find(User.class, user.getId());
        var token = RefreshToken.builder()
                .token("random-token")
                .user(savedUser)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        var duplicateToken = RefreshToken.builder()
                .token("random-token")
                .user(savedUser)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        entityManager.persist(token);
        entityManager.flush();
        entityManager.persist(duplicateToken);
        assertThatThrownBy(entityManager::flush)
                .isInstanceOf(ConstraintViolationException.class);
    }
}