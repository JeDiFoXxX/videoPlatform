package ru.videoplatform.auth.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.model.User;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Должен успешно найти администратора из Liquibase сида и проверить его роль")
    void shouldFindSeedAdminUserAndVerifyRole() {
        var optional = userRepository.findByLogin("admin");
        assertThat(optional)
                .isPresent()
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Должен вернуть true при проверке существования логина")
    void shouldReturnTrueWhenAdminExistsByLogin() {
        var value = userRepository.existsByLogin("admin");
        assertThat(value).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false при проверке несуществующего логина")
    void shouldReturnFalseWhenUserDoesNotExistByLogin() {
        var value = userRepository.existsByLogin("teacher");
        assertThat(value).isFalse();
    }
}