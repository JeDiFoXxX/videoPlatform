package ru.videoplatform.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.videoplatform.auth.dto.request.LoginDto;
import ru.videoplatform.auth.dto.request.RefreshDto;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.dto.response.AuthResponseDto;
import ru.videoplatform.auth.exception.AuthException;
import ru.videoplatform.auth.model.BlacklistedToken;
import ru.videoplatform.auth.model.RefreshToken;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.repository.BlacklistedTokenRepository;
import ru.videoplatform.auth.repository.RefreshTokenRepository;
import ru.videoplatform.auth.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Transactional
    public User registerStudent(RegisterDto dto) {
        return createUser(dto.getLogin(), dto.getPassword(), UserRole.STUDENT);
    }

    @Transactional
    public User registerTeacher(TeacherRegisterDto dto) {
        return createUser(dto.getLogin(), dto.getPassword(), UserRole.TEACHER);
    }

    @Transactional
    public AuthResponseDto login(LoginDto dto) {
        var user = userRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> AuthException.unauthorized("Пользователь с таким логином не найден"));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw AuthException.unauthorized("Введен неверный пароль");
        }
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .build();
        var savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(savedRefreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthResponseDto refresh(RefreshDto dto) {
        var session = refreshTokenRepository.findByTokenAndRevokedFalse(dto.getRefreshToken())
                .orElseThrow(() -> AuthException.unauthorized("Сессия не найдена или была отозвана"));
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.unauthorized("Сессия истекла");
        }
        var newAccessToken = jwtService.generateAccessToken(session.getUser());
        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(session.getToken())
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(session -> {
            var updatedSession = session.toBuilder()
                    .revoked(true)
                    .build();
            refreshTokenRepository.save(updatedSession);
        });
        var jti = jwtService.extractJti(accessToken);
        if (jti != null) {
            var blacklist = BlacklistedToken.builder()
                    .jti(jti)
                    .expiresAt(Instant.now().plusSeconds(900))
                    .build();
            blacklistedTokenRepository.save(blacklist);
        }
    }

    private User createUser(String login, String password, UserRole role) {
        validateLoginUniqueness(login);
        var user = User.builder()
                .login(login)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private void validateLoginUniqueness(String login) {
        if (userRepository.existsByLogin(login)) {
            throw AuthException.conflict("Логин '" + login + "' уже занят");
        }
    }
}