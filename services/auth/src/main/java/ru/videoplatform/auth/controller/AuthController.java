package ru.videoplatform.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RefreshDto;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.dto.response.AuthResponseDto;
import ru.videoplatform.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterDto dto) {
        authService.registerStudent(dto);
    }

    @PostMapping("/register/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTeacher(@Valid @RequestBody TeacherRegisterDto dto) {
        authService.registerTeacher(dto);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshDto dto) {
        return authService.refresh(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RefreshDto dto) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Отсутствует или неверно указан заголовок Authorization");
        }
        var accessToken = authorization.substring(7);
        authService.logout(accessToken, dto.getRefreshToken());
    }
}
