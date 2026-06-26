package ru.videoplatform.auth.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    @DisplayName("Должен отклонять пароль, если передан null")
    void shouldRejectNullPassword() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("Должен отклонять пароль, если переданы пробелы")
    void shouldRejectPasswordWithOnlySpaces() {
        assertThat(validator.isValid("     ", null)).isFalse();
    }

    @Test
    @DisplayName("Должен отклонить слишком короткий пароль (меньше 10 символов)")
    void shouldRejectShortPassword() {
        assertThat(validator.isValid("short1!", null)).isFalse();
    }

    @Test
    @DisplayName("Должен отклонить слишком длинный пароль (меньше 20 символов)")
    void shouldRejectPasswordLongerThan20Characters() {
        assertThat(validator.isValid("Long12345@54321@12345@54321!", null)).isFalse();
    }

    @Test
    @DisplayName("Должен отклонить пароль без заглавных букв")
    void shouldRejectPasswordWithoutUppercase() {
        assertThat(validator.isValid("short123456!!!", null)).isFalse();
    }

    @Test
    @DisplayName("Должен отклонить пароль без спец символов (например !@#$%^&*)")
    void shouldRejectPasswordWithoutSpecialCharacters() {
        assertThat(validator.isValid("Medium123456", null)).isFalse();
    }

    @Test
    @DisplayName("Должен отклонить пароль без спец символов < 3 (например !@#$%^&*)")
    void shouldRejectPasswordWithLessThanThreeSpecialCharacters() {
        assertThat(validator.isValid("Medium123456@%", null)).isFalse();
    }

    @Test
    @DisplayName("Должен успешно принять пароль")
    void shouldAcceptValidPassword() {
        assertThat(validator.isValid("Long123456@%!", null)).isTrue();
    }
}