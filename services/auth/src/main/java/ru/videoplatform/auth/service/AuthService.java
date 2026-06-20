package ru.videoplatform.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.exception.AuthException;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerStudent(RegisterDto dto) {
        return createNewUser(dto.getLogin(), dto.getPassword(), UserRole.STUDENT);
    }

    @Transactional
    public User registerTeacher(TeacherRegisterDto dto) {
        return createNewUser(dto.getLogin(), dto.getPassword(), UserRole.TEACHER);
    }

    private User createNewUser(String login, String rawPassword, UserRole role) {
        validateLoginUniqueness(login);
        var user = User.builder()
                .login(login)
                .passwordHash(passwordEncoder.encode(rawPassword))
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