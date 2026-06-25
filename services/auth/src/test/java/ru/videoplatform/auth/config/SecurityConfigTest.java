package ru.videoplatform.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.videoplatform.auth.model.BlacklistedToken;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.repository.BlacklistedTokenRepository;
import ru.videoplatform.auth.service.JwtService;
import ru.videoplatform.auth.support.SecurityTestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityTestController.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Test
    @DisplayName("POST /register без токена — 201 (permitAll)")
    void registerWithoutTokenShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /register/teacher с ADMIN JWT — 201")
    void registerTeacherWithAdminTokenShouldReturnCreated() throws Exception {
        var token = jwtService.generateAccessToken(adminUser());
        mockMvc.perform(post("/api/v1/auth/register/teacher")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /register/teacher без токена — 403")
    void registerTeacherWithoutTokenShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /logout с blacklisted access — 401")
    void blacklistedTokenShouldReturnUnauthorized() throws Exception {
        var token = jwtService.generateAccessToken(adminUser());
        var jti = jwtService.extractJti(token);
        blacklistedTokenRepository.save(BlacklistedToken.builder()
                .jti(jti)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build());
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private User adminUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .role(UserRole.ADMIN)
                .build();
    }
}