package ru.videoplatform.auth.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;

class AuthResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Должен правильно сериализовать AuthResponseDto в JSON")
    void shouldSerializeAuthResponseDtoToSnakeCaseJson() throws Exception {
        var dto = AuthResponseDto.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("rfr_987654321_xyz")
                .build();
        var jsonResult = objectMapper.writeValueAsString(dto);
        assertThat(jsonResult).contains("\"type\":\"auth_success\"");
        assertThat(jsonResult).contains("\"access_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"");
        assertThat(jsonResult).contains("\"refresh_token\":\"rfr_987654321_xyz\"");
        assertThat(jsonResult).contains("\"expires_in\":900");
    }
}