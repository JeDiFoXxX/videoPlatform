package ru.videoplatform.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RefreshDto;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.dto.response.AuthResponseDto;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.service.AuthService;
import ru.videoplatform.auth.service.JwtService;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /register — успешная регистрация студента возвращает 201")
    void registerWithValidDtoShouldReturnCreated() throws Exception {
        var validDto = createStudentDto();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
        verify(authService).registerStudent(any());
    }

    @Test
    @DisplayName("POST /register/teacher — успешная регистрация преподавателя админом возвращает 201")
    void registerTeacherWithValidDtoAndAdminRoleShouldReturnCreated() throws Exception {
        var validTeacherDto = createTeacherDto();
        var adminToken = jwtService.generateAccessToken(createUser(UserRole.ADMIN));
        mockMvc.perform(post("/api/v1/auth/register/teacher")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeacherDto)))
                .andExpect(status().isCreated());
        verify(authService).registerTeacher(any());
    }

    @Test
    @DisplayName("POST /refresh — успешное обновление возвращает 201 и новый токен")
    void refreshTokensShouldReturnOkAndNewTokens() throws Exception {
        var expectedResponse = AuthResponseDto.builder()
                .accessToken("new_hashed_access_token")
                .build();
        doReturn(expectedResponse).when(authService).refresh(any());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRefreshDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new_hashed_access_token"));
        verify(authService).refresh(any());
    }

    @Test
    @DisplayName("POST /login — успешный вход возвращает 200")
    void loginWithValidCredentialsShouldReturnTokens() throws Exception {
        var dto = createLoginDto();
        var expectedResponse = AuthResponseDto.builder()
                .accessToken("hashed_access_token")
                .refreshToken("hashed_refresh_token")
                .build();
        doReturn(expectedResponse).when(authService).login(any());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("hashed_access_token"))
                .andExpect(jsonPath("$.refresh_token").value("hashed_refresh_token"));
    }

    @Test
    @DisplayName("POST /logout — успешный выход возвращает 204")
    void shouldReturnNoContentWhenLogoutIsSuccessful() throws Exception {
        var accessToken = jwtService.generateAccessToken(createUser(UserRole.STUDENT));
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRefreshDto())))
                .andExpect(status().isNoContent());

        verify(authService).logout(anyString(), anyString());
    }

    private RegisterDto createStudentDto() {
        return new RegisterDto("student", "validPassword123!@@");
    }

    private TeacherRegisterDto createTeacherDto() {
        return new TeacherRegisterDto("teacher", "validPassword123!@@");
    }

    private LoginDto createLoginDto() {
        return new LoginDto("admin", "validPassword123!@@");
    }

    private RefreshDto createRefreshDto() {
        return new RefreshDto("hashed_refresh_token");
    }

    private User createUser(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .login(role.name().toLowerCase())
                .role(role)
                .build();
    }
}