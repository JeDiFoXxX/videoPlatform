package ru.videoplatform.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.videoplatform.booking.dto.LessonCreateDto;
import ru.videoplatform.booking.exception.SlotConflictException;
import ru.videoplatform.booking.model.Lesson;
import ru.videoplatform.booking.model.LessonStatus;
import ru.videoplatform.booking.repository.LessonRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonService lessonService;

    private LessonCreateDto validDto;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.now().truncatedTo(ChronoUnit.HOURS);
        validDto = new LessonCreateDto(
                "student_1",
                baseTime,
                baseTime.plus(60, ChronoUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Успешное создание урока, когда слот абсолютно свободен")
    void shouldCreateLessonSuccessfully() {
        var generatedId = UUID.randomUUID();
        given(lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(any(), any(), any()))
                .willReturn(false);
        given(lessonRepository.save(any())).willReturn(
                Lesson.builder()
                        .id(generatedId)
                        .studentId("student_1")
                        .startTime(baseTime)
                        .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                        .status(LessonStatus.SCHEDULED)
                        .build()
        );
        var response = lessonService.createLesson(validDto);
        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("student_1", response.studentId());
        assertEquals("SCHEDULED", response.status());
        verify(lessonRepository, times(1))
                .existsByStatusInAndEndTimeAfterAndStartTimeBefore(any(), any(), any());
        verify(lessonRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException, "
            + "если existsByStatusInAndEndTimeAfterAndStartTimeBefore вернул true")
    void shouldThrowSlotConflictExceptionWhenSlotIsOccupied() {
        given(lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(any(), any(), any()))
                .willReturn(true);
        var exception = assertThrows(SlotConflictException.class, () ->
                lessonService.createLesson(validDto)
        );
        assertEquals("Слот уже занят", exception.getMessage());
        verify(lessonRepository, times(1))
                .existsByStatusInAndEndTimeAfterAndStartTimeBefore(any(), any(), any());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException, если existsByStatusInAndStudentId вернул true")
    void shouldThrowSlotConflictExceptionWhenStudentAlreadyHasActiveLesson() {
        given(lessonRepository.existsByStatusInAndStudentId(any(), any()))
                .willReturn(true);
        var exception = assertThrows(SlotConflictException.class, () ->
                lessonService.createLesson(validDto)
        );
        assertEquals("Сначала отмените текущий урок", exception.getMessage());
        verify(lessonRepository, times(1))
                .existsByStatusInAndStudentId(any(), any());
        verify(lessonRepository, never()).existsByStatusInAndEndTimeAfterAndStartTimeBefore(any(), any(), any());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешная отмена урока (перевод в статус CANCELED)")
    void shouldCancelLessonSuccessfully() {
        var lessonId = UUID.randomUUID();
        var studentId = "student_1";
        var activeLesson = Lesson.builder()
                .id(lessonId)
                .studentId(studentId)
                .status(LessonStatus.SCHEDULED)
                .build();
        var savedLesson = activeLesson.toBuilder()
                .status(LessonStatus.CANCELED)
                .build();
        given(lessonRepository.findByStatusAndIdAndStudentId(LessonStatus.SCHEDULED, lessonId, studentId))
                .willReturn(Optional.of(activeLesson));
        given(lessonRepository.save(any())).willReturn(savedLesson);
        assertNotNull(lessonService.cancelLesson(lessonId, studentId));
        verify(lessonRepository, times(1))
                .findByStatusAndIdAndStudentId(LessonStatus.SCHEDULED, lessonId, studentId);
        verify(lessonRepository, times(1))
                .save(argThat(lesson -> lesson.getStatus() == LessonStatus.CANCELED));
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException, если урок не найден или недоступен для отмены")
    void shouldThrowSlotConflictExceptionWhenLessonNotFoundOrUnavailable() {
        var lessonId = UUID.randomUUID();
        var studentId = "student_1";
        given(lessonRepository.findByStatusAndIdAndStudentId(any(), any(), any()))
                .willReturn(Optional.empty());
        var exception = assertThrows(SlotConflictException.class, () ->
                lessonService.cancelLesson(lessonId, studentId)
        );
        assertEquals("Не удалось отменить урок", exception.getMessage());
        verify(lessonRepository, times(1))
                .findByStatusAndIdAndStudentId(any(), any(), any());
        verify(lessonRepository, never()).save(any());
    }
}
