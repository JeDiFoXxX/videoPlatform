package ru.videoplatform.auth.support;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RefreshDto;
import ru.videoplatform.auth.dto.request.RegisterDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AllArgsConstructor
public class AuthTestClient {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    public ResultActions register(String login, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterDto(login, password))));
    }

    public ResultActions registerTeacher(String login,
                                         String password,
                                         String adminAccessToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register/teacher")
                .header("Authorization", "Bearer " + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterDto(login, password))));
    }

    public ResultActions login(String login, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginDto(login, password))));
    }

    public ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshDto(refreshToken))));
    }

    public ResultActions logout(String accessToken, String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshDto(refreshToken))));
    }
}
