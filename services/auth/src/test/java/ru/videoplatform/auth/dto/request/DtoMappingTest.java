package ru.videoplatform.auth.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Должен успешно десериализовать RegisterDto для студентов")
    void shouldDeserializeRegisterDto() throws Exception {
        var json = "{\"login\":\"student01\",\"password\":\"Str0ng!!!Pass\"}";
        var dto = objectMapper.readValue(json, RegisterDto.class);
        assertThat(dto.getLogin()).isEqualTo("student01");
        assertThat(dto.getPassword()).isEqualTo("Str0ng!!!Pass");
    }

    @Test
    @DisplayName("Должен успешно десериализовать TeacherRegisterDto для преподавателей")
    void shouldDeserializeTeacherRegisterDto() throws Exception {
        var json = "{\"login\":\"teacher123\",\"password\":\"Pass123\"}";
        var dto = objectMapper.readValue(json, TeacherRegisterDto.class);
        assertThat(dto.getLogin()).isEqualTo("teacher123");
        assertThat(dto.getPassword()).isEqualTo("Pass123");
    }

    @Test
    @DisplayName("Должен успешно десериализовать LoginDto для аутентификации")
    void shouldDeserializeLoginDto() throws Exception {
        var json = "{\"login\":\"teacher123\",\"password\":\"Str0ng!!!Pass\"}";
        var dto = objectMapper.readValue(json, LoginDto.class);
        assertThat(dto.getLogin()).isEqualTo("teacher123");
        assertThat(dto.getPassword()).isEqualTo("Str0ng!!!Pass");
    }

    @Test
    @DisplayName("Должен успешно десериализовать RefreshDto с поддержкой snake_case")
    void shouldDeserializeRefreshDtoWithSnakeCase() throws Exception {
        var json = "{\"refresh_token\":\"rfr_987654321_xyz\"}";
        var dto = objectMapper.readValue(json, RefreshDto.class);
        assertThat(dto.getRefreshToken()).isEqualTo("rfr_987654321_xyz");
    }
}
