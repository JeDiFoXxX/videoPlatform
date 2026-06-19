package ru.videoplatform.auth.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BlacklistedTokenTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Должен сохранять и проверять ограничения BlacklistedToken")
    void shouldSaveBlacklistedToken() {
        var blacklisted = BlacklistedToken.builder()
                .jti(UUID.randomUUID().toString())
                .expiresAt(Instant.now().truncatedTo(ChronoUnit.MILLIS).plus(15, ChronoUnit.MINUTES))
                .build();
        var savedBlacklisted = entityManager.persistFlushFind(blacklisted);
        assertThat(savedBlacklisted.getId()).isNotNull();
        assertThat(blacklisted.getJti()).isEqualTo(savedBlacklisted.getJti());
        assertThat(blacklisted.getExpiresAt()).isEqualTo(savedBlacklisted.getExpiresAt());
    }
}