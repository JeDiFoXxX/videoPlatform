package ru.videoplatform.auth.service;

import org.junit.jupiter.api.BeforeEach;
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

    private RegisterDto studentDto;
    private TeacherRegisterDto teacherDto;
    private LoginDto loginDto;
    private RefreshDto refreshDto;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        studentDto = new RegisterDto("student123", "valid_password");
        teacherDto = new TeacherRegisterDto("teacher123", "valid_password");
        loginDto = new LoginDto("user123", "user_password");
        refreshDto = new RefreshDto("hashed_refresh_token");
        user = User.builder()
                .login("student123")
                .passwordHash("hashed_student_password")
                .role(UserRole.STUDENT)
                .build();
        refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Успешная регистрация STUDENT")
    void shouldRegisterStudentSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(anyString());
        doReturn("hashed_student_password").when(passwordEncoder).encode(anyString());
        doReturn(user).when(userRepository).save(any());
        var result = authService.registerStudent(studentDto);
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
        doReturn(true).when(userRepository).existsByLogin(anyString());
        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин '" + studentDto.getLogin() + "' уже занят");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная регистрация TEACHER")
    void shouldRegisterTeacherSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(anyString());
        doReturn("hashed_teacher_password").when(passwordEncoder).encode(anyString());
        var savedTeacher = User.builder()
                .login(teacherDto.getLogin())
                .passwordHash("hashed_teacher_password")
                .role(UserRole.TEACHER)
                .build();
        doReturn(savedTeacher).when(userRepository).save(any());
        var result = authService.registerTeacher(teacherDto);
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
        doReturn(true).when(userRepository).existsByLogin(anyString());
        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин '" + teacherDto.getLogin() + "' уже занят");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная авторизация пользователя с генерацией JWT и Refresh токенов")
    void shouldLoginSuccessfully() {
        doReturn(Optional.of(user)).when(userRepository).findByLogin(anyString());
        doReturn(true).when(passwordEncoder).matches(anyString(), anyString());
        doReturn("hashed_access_token").when(jwtService).generateAccessToken(any());
        doReturn(refreshToken).when(refreshTokenRepository).save(any());
        var result = authService.login(loginDto);
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
        doReturn(Optional.of(refreshToken)).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        doReturn("hashed_access_token").when(jwtService).generateAccessToken(any());
        var result = authService.refresh(refreshDto);
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
        verify(jwtService).generateAccessToken(any());
        assertThat(result.getAccessToken()).isEqualTo("hashed_access_token");
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(result.getExpiresIn()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Успешный логаут пользователя с отзывом сессии и баном токена")
    void shouldLogoutSuccessfully() {
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
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Пользователь с таким логином не найден");
        verify(userRepository).findByLogin(anyString());
    }

    @Test
    @DisplayName("Выброс исключения, если введен неверный пароль")
    void loginShouldThrowExceptionWhenPasswordIsIncorrect() {
        doReturn(Optional.of(user)).when(userRepository).findByLogin(anyString());
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());
        assertThatThrownBy(() -> authService.login(loginDto))
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
        assertThatThrownBy(() -> authService.refresh(refreshDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Сессия не найдена или была отозвана");
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
    }

    @Test
    @DisplayName("Выброс исключения, если срок действия сессии истек")
    void refreshShouldThrowExceptionWhenSessionHasExpired() {
        refreshToken = RefreshToken.builder()
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        doReturn(Optional.of(refreshToken)).when(refreshTokenRepository)
                .findByTokenAndRevokedFalse(anyString());
        assertThatThrownBy(() -> authService.refresh(refreshDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Сессия истекла");
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(anyString());
    }
}