package ru.videoplatform.auth.model;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Должен успешно сохранять пользователя в таблицу Liquibase и читать его")
    void shouldPersistAndReadUserWithLiquibaseSchema() {
        var user = User.builder()
                .login("teacherLogin")
                .passwordHash("hashPassword")
                .role(UserRole.TEACHER)
                .build();
        var savedUser = entityManager.persistFlushFind(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(savedUser.getPasswordHash()).isEqualTo(user.getPasswordHash());
        assertThat(savedUser.getRole()).isEqualTo(user.getRole());
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен отклонять дубликат login")
    void shouldRejectDuplicateLogin() {
        entityManager.persistAndFlush(User.builder()
                .login("duplicateLogin")
                .passwordHash("hash1")
                .role(UserRole.STUDENT)
                .build());
        entityManager.persist(User.builder()
                .login("duplicateLogin")
                .passwordHash("hash2")
                .role(UserRole.STUDENT)
                .build());
        assertThatThrownBy(entityManager::flush)
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Должен отклонять login длиннее 20 символов")
    void shouldRejectLoginLongerThan20Characters() {
        var login = "a".repeat(21);
        entityManager.persist(User.builder()
                .login(login)
                .passwordHash("hashPassword")
                .role(UserRole.STUDENT)
                .build());
        assertThatThrownBy(entityManager::flush)
                .isInstanceOf(PersistenceException.class);
    }
}
