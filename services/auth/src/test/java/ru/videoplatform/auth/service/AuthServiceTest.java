package ru.videoplatform.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.videoplatform.auth.config.JwtProperties;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RefreshDto;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.model.RefreshToken;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.repository.BlacklistedTokenRepository;
import ru.videoplatform.auth.repository.RefreshTokenRepository;
import ru.videoplatform.auth.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Spy
    private JwtProperties jwtProperties = new JwtProperties();

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация STUDENT")
    void shouldRegisterStudentSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(anyString());
        doReturn("hashed_student_password").when(passwordEncoder).encode(anyString());
        doReturn(createUser(UserRole.STUDENT)).when(userRepository).save(any());
        var result = authService.registerStudent(createStudentDto());
        verify(userRepository).existsByLogin(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any());
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(result.getPasswordHash()).isEqualTo("hashed_student_password");
    }

    @Test
    @DisplayName("Ошибка при регистрации STUDENT с дублирующимся логином")
    void shouldThrowConflictWhenStudentLoginExists() {
        var studentDto = createStudentDto();
        doReturn(true).when(userRepository).existsByLogin(anyString());
        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин " + studentDto.getLogin() + " уже занят");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная регистрация TEACHER")
    void shouldRegisterTeacherSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(anyString());
        doReturn("hashed_teacher_password").when(passwordEncoder).encode(anyString());
        doReturn(createUser(UserRole.TEACHER)).when(userRepository).save(any());
        var result = authService.registerTeacher(createTeacherDto());
        verify(userRepository).existsByLogin(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any());
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(result.getPasswordHash()).isEqualTo("hashed_teacher_password");
    }

    @Test
    @DisplayName("Ошибка при регистрации TEACHER с дублирующимся логином")
    void shouldThrowConflictWhenTeacherLoginExists() {
        var teacherDto = createTeacherDto();
        doReturn(true).when(userRepository).existsByLogin(anyString());
        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин " + teacherDto.getLogin() + " уже занят");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная авторизация пользователя с генерацией JWT и Refresh токенов")
    void shouldLoginSuccessfully() {
        var refreshToken = createValidRefreshToken();
        doReturn(Optional.of(createUser(UserRole.STUDENT))).when(userRepository).findByLogin(anyString());
        doReturn(true).when(passwordEncoder).matches(anyString(), anyString());
        doReturn("hashed_access_token").when(jwtService).generateAccessToken(any());
        doReturn(refreshToken).when(refreshTokenRepository).save(any());
        var result = authService.login(createLoginDto());
        verify(userRepository).findByLogin(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtService).generateAccessToken(any());
        verify(refreshTokenRepository).save(any());
        assertThat(result.getAccessToken()).isEqualTo("hashed_access_token");
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(result.getExpiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Успешное обновление пары токенов по валидному Refresh токену")
    void shouldRefreshTokensSuccessfully() {
        var refreshToken = createValidRefreshToken();
        doReturn(Optional.of(refreshToken)).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        doReturn("hashed_access_token").when(jwtService).generateAccessToken(any());
        var result = authService.refresh(createRefreshDto());
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
        verify(jwtService).generateAccessToken(any());
        assertThat(result.getAccessToken()).isEqualTo("hashed_access_token");
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(result.getExpiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Успешный логаут пользователя с отзывом сессии и баном токена")
    void shouldLogoutSuccessfully() {
        var refreshToken = createValidRefreshToken();
        doReturn(Optional.of(refreshToken)).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        doReturn("mocked-jti-uuid").when(jwtService).extractJti(anyString());
        authService.logout("hashed_access_token", refreshToken.getToken());
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
        verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
        verify(jwtService).extractJti(anyString());
        verify(blacklistedTokenRepository).save(any());
    }

    @Test
    @DisplayName("Выброс исключения, если пользователь с таким логином не найден")
    void loginShouldThrowExceptionWhenUserDoesNotExist() {
        doReturn(Optional.empty()).when(userRepository).findByLogin(anyString());
        assertThatThrownBy(() -> authService.login(createLoginDto()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Пользователь с таким логином не найден");
        verify(userRepository).findByLogin(anyString());
    }

    @Test
    @DisplayName("Выброс исключения, если введен неверный пароль")
    void loginShouldThrowExceptionWhenPasswordIsIncorrect() {
        doReturn(Optional.of(createUser(UserRole.STUDENT))).when(userRepository).findByLogin(anyString());
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());
        assertThatThrownBy(() -> authService.login(createLoginDto()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Введен неверный пароль");
        verify(userRepository).findByLogin(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Выброс исключения, если сессия не найдена или была отозвана")
    void refreshShouldThrowExceptionWhenSessionNotFoundOrRevoked() {
        doReturn(Optional.empty()).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        assertThatThrownBy(() -> authService.refresh(createRefreshDto()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Сессия не найдена или была отозвана");
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
    }

    @Test
    @DisplayName("Выброс исключения, если срок действия сессии истек")
    void refreshShouldThrowExceptionWhenSessionHasExpired() {
        doReturn(Optional.of(createInvalidRefreshToken())).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        assertThatThrownBy(() -> authService.refresh(createRefreshDto()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Сессия истекла");
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
    }

    private RegisterDto createStudentDto() {
        return new RegisterDto("student", "validPassword123!@@rd");
    }

    private TeacherRegisterDto createTeacherDto() {
        return new TeacherRegisterDto("teacher", "validPassword123!@@rd");
    }

    private LoginDto createLoginDto() {
        return new LoginDto("user", "validPassword123!@@");
    }

    private RefreshDto createRefreshDto() {
        return new RefreshDto("hashed_refresh_token");
    }

    private User createUser(UserRole role) {
        return User.builder()
                .login(role.name().toLowerCase())
                .passwordHash("hashed_" + role.name().toLowerCase() +"_password")
                .role(role)
                .build();
    }

    private RefreshToken createValidRefreshToken() {
        return RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .user(createUser(UserRole.STUDENT))
                .build();
    }

    private RefreshToken createInvalidRefreshToken() {
        return RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .user(createUser(UserRole.STUDENT))
                .build();
    }
}