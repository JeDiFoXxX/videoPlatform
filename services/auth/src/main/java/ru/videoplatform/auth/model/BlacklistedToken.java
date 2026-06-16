package ru.videoplatform.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_blacklisted_tokens_jti", columnList = "jti", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
