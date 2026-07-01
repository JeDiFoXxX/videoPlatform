package ru.videoplatform.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import ru.videoplatform.auth.repository.BlacklistedTokenRepository;
import ru.videoplatform.auth.service.JwtService;
import ru.videoplatform.auth.support.AuthTestClient;
import ru.videoplatform.auth.support.BaseIntegrationTest;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private AuthTestClient authTestClient;

    @BeforeEach
    void setUpClient() {
        this.authTestClient = new AuthTestClient(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Полный цикл аутентификации (register -> login -> refresh -> logout)")
    void fullAuthCycleScenario() throws Exception {
        var login = "student";
        var password = "validPassword123!@@";
        authTestClient.register(login, password).andExpect(status().isCreated());
        var loginResponse = authTestClient.login(login, password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andReturn().getResponse().getContentAsString();
        var refreshToken = objectMapper.readTree(loginResponse).get("refresh_token").asString();
        var refreshResponse = authTestClient.refresh(refreshToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn().getResponse().getContentAsString();
        var newAccessToken = objectMapper.readTree(refreshResponse).get("access_token").asString();
        authTestClient.logout(newAccessToken, refreshToken).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Регистрация преподавателя администратором")
    void teacherRegistrationByAdminScenario() throws Exception {
        var adminLoginResponse = authTestClient.login("admin", ADMIN_PASSWORD_TEST)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var adminAccessToken = objectMapper.readTree(adminLoginResponse).get("access_token").asString();
        var login = "teacher";
        var password = "validPassword123!@@";
        authTestClient.registerTeacher(login, password, adminAccessToken).andExpect(status().isCreated());
        authTestClient.login(login, password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists());
    }

    @Test
    @DisplayName("Попадание токена в Blacklist после Logout")
    void tokenBlacklistScenario() throws Exception {
        var login = "student";
        var password = "validPassword123!@@";
        authTestClient.register(login, password).andExpect(status().isCreated());
        var loginResponse = authTestClient.login(login, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var accessToken = objectMapper.readTree(loginResponse).get("access_token").asString();
        var refreshToken = objectMapper.readTree(loginResponse).get("refresh_token").asString();
        var jti = jwtService.extractJti(accessToken);
        authTestClient.logout(accessToken, refreshToken).andExpect(status().isNoContent());
        assertThat(blacklistedTokenRepository.existsByJti(jti)).isTrue();
    }
}