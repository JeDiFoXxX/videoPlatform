package ru.videoplatform.auth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.service.AuthService;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private AuthService authService;

    @Test
    @DisplayName("Ошибка валидации длины логина возвращает статус 400")
    void validationErrorReturns400WithErrorResponse() throws Exception {
        var badLogin = createRegisterDto("student".repeat(3), "validPassword1!@@");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Логин не должен превышать 20 символов")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));
    }

    @Test
    @DisplayName("Пустой пароль перехватывается аннотацией @NotBlank и возвращает статус 400")
    void blankPasswordReturns400WithErrorResponse() throws Exception {
        var badPassword = createRegisterDto("student", null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Пароль не может быть пустым")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));
    }

    @Test
    @DisplayName("Попытка входа с несуществующим логином возвращает статус 401")
    void notExistingLoginReturns401WithErrorResponse() throws Exception {
        var notExistingLogin = createLoginDto();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notExistingLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Пользователь с таким логином не найден")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));
    }

    @Test
    @DisplayName("Регистрация уже существующего логина возвращает статус 409")
    void duplicateLoginReturns409WithErrorResponse() throws Exception {
        var duplicateLogin = createRegisterDto("admin", "validPassword1!@@");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateLogin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Логин admin уже занят")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));
    }

    @Test
    @DisplayName("Непредвиденное исключение при регистрации возвращает статус 500")
    void internalServerErrorReturns500WithErrorResponse() throws Exception {
        doThrow(new RuntimeException()).when(authService).registerStudent(any());
        var registerDto = createRegisterDto("student", "studentPassword1!@@");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Внутренняя ошибка сервера")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));
    }

    private RegisterDto createRegisterDto(String login, String password) {
        return new RegisterDto(login, password);
    }

    private LoginDto createLoginDto() {
        return new LoginDto("student", "studentPassword1!@@");
    }
}