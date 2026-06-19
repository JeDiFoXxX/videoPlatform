package ru.videoplatform.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.videoplatform.auth.model.BlacklistedToken;

import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

    boolean existsByJti(String jti);
}
