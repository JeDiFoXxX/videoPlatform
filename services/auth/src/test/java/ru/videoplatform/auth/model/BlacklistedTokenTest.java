package ru.videoplatform.auth.model;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BlacklistedTokenTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Должен сохранять и проверять ограничения BlacklistedToken")
    void shouldSaveBlacklistedToken() {
        var blacklisted = BlacklistedToken.builder()
                .jti(UUID.randomUUID().toString())
                .expiresAt(Instant.now().truncatedTo(ChronoUnit.MILLIS).plus(15, ChronoUnit.MINUTES))
                .build();
        entityManager.persist(blacklisted);
        entityManager.flush();
        entityManager.clear();
        var savedBlacklisted = entityManager.find(BlacklistedToken.class, blacklisted.getId());
        assertThat(savedBlacklisted.getId()).isNotNull();
        assertThat(blacklisted.getJti()).isEqualTo(savedBlacklisted.getJti());
        assertThat(blacklisted.getExpiresAt()).isEqualTo(savedBlacklisted.getExpiresAt());
    }
}