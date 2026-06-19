package ru.videoplatform.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.videoplatform.auth.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);
}
