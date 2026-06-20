package ru.videoplatform.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.videoplatform.auth.dto.request.RegisterDto;
import ru.videoplatform.auth.dto.request.TeacherRegisterDto;
import ru.videoplatform.auth.model.User;
import ru.videoplatform.auth.model.UserRole;
import ru.videoplatform.auth.repository.UserRepository;

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

    @InjectMocks
    private AuthService authService;

    private RegisterDto studentDto;
    private TeacherRegisterDto teacherDto;

    @BeforeEach
    void setUp() {
        studentDto = new RegisterDto("student123", "ValidPassword123!@#");
        teacherDto = new TeacherRegisterDto("teacher123", "ValidPassword123!@#");
    }

    @Test
    @DisplayName("Успешная регистрация STUDENT")
    void shouldRegisterStudentSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(studentDto.getLogin());
        doReturn("hashed_password").when(passwordEncoder).encode(studentDto.getPassword());
        var savedUser = User.builder()
                .id(UUID.randomUUID())
                .login(studentDto.getLogin())
                .passwordHash("hashed_student_password")
                .role(UserRole.STUDENT)
                .build();
        doReturn(savedUser).when(userRepository).save(any(User.class));
        var result = authService.registerStudent(studentDto);
        verify(userRepository).existsByLogin(studentDto.getLogin());
        verify(passwordEncoder).encode(studentDto.getPassword());
        verify(userRepository).save(any(User.class));
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(result.getPasswordHash()).isEqualTo("hashed_student_password");
    }

    @Test
    @DisplayName("Ошибка при регистрации STUDENT с дублирующимся логином")
    void shouldThrowConflictWhenStudentLoginExists() {
        doReturn(true).when(userRepository).existsByLogin(studentDto.getLogin());
        assertThatThrownBy(() -> authService.registerStudent(studentDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин '" + studentDto.getLogin() + "' уже занят");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная регистрация TEACHER")
    void shouldRegisterTeacherSuccessfully() {
        doReturn(false).when(userRepository).existsByLogin(teacherDto.getLogin());
        doReturn("hashed_teacher_password").when(passwordEncoder).encode(teacherDto.getPassword());
        var savedTeacher = User.builder()
                .id(UUID.randomUUID())
                .login(teacherDto.getLogin())
                .passwordHash("hashed_teacher_password")
                .role(UserRole.TEACHER)
                .build();
        doReturn(savedTeacher).when(userRepository).save(any(User.class));
        var result = authService.registerTeacher(teacherDto);
        verify(userRepository).save(any(User.class));
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.TEACHER);
    }

    @Test
    @DisplayName("Ошибка при регистрации TEACHER с дублирующимся логином")
    void shouldThrowConflictWhenTeacherLoginExists() {
        doReturn(true).when(userRepository).existsByLogin(teacherDto.getLogin());
        assertThatThrownBy(() -> authService.registerTeacher(teacherDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Логин '" + teacherDto.getLogin() + "' уже занят");
        verify(userRepository, never()).save(any());
    }
}