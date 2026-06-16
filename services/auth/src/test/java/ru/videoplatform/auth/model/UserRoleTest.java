package ru.videoplatform.auth.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
class UserRoleTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Должен успешно парсить роли из строк")
    void shouldParseEnumFromString() {
        assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(UserRole.valueOf("TEACHER")).isEqualTo(UserRole.TEACHER);
        assertThat(UserRole.valueOf("STUDENT")).isEqualTo(UserRole.STUDENT);
    }

    @Test
    @DisplayName("Должен выбрасывать IllegalArgumentException для неизвестной роли")
    void shouldRejectUnknownRoleValue() {
        assertThatThrownBy(() -> UserRole.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Должен правильно сериализовать Enum в строку JSON")
    void shouldSerializeEnumToJsonString() throws Exception {
        String json = objectMapper.writeValueAsString(UserRole.ADMIN);
        assertThat(json).isEqualTo("\"ADMIN\"");
    }

    @Test
    @DisplayName("Должен правильно десериализовать Enum из строки JSON")
    void shouldDeserializeEnumFromJsonString() throws Exception {
        UserRole role = objectMapper.readValue("\"STUDENT\"", UserRole.class);
        assertThat(role).isEqualTo(UserRole.STUDENT);
    }
}
