package ru.videoplatform.auth.model;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Должен успешно сохранять пользователя в таблицу Liquibase и читать его")
    void shouldPersistAndReadUserWithLiquibaseSchema() {
        var user = User.builder()
                .login("teacherLogin")
                .password("hashPassword")
                .role(UserRole.TEACHER)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();
        var savedUser = entityManager.find(User.class, user.getId());
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(savedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(savedUser.getRole()).isEqualTo(user.getRole());
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }
}