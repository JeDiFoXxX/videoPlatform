package ru.videoplatform.booking.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.videoplatform.booking.model.Lesson;
import ru.videoplatform.booking.model.LessonStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        lessonRepository.deleteAll();
        baseTime = Instant.now().truncatedTo(ChronoUnit.HOURS);
    }

    @Test
    @DisplayName("Должен генерировать id при сохранении урока в БД")
    void shouldGenerateIdWhenLessonIsPersisted() {
        var lessonToSave = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        var savedLesson = entityManager.persistAndFlush(lessonToSave);
        assertNotNull(savedLesson.getId());
    }

    @Test
    @DisplayName("Должен обнаружить пересечение, если новый урок накладывается на существующий")
    void shouldDetectOverlapWhenLessonsIntersects() {
        var existingLesson = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        entityManager.persistAndFlush(existingLesson);
        Instant newStart = baseTime.plus(30, ChronoUnit.MINUTES);
        Instant newEnd = baseTime.plus(90, ChronoUnit.MINUTES);
        var result = lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(
                List.of(LessonStatus.SCHEDULED), newStart, newEnd
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("Не должен находить пересечение, если уроки идут строго подряд (минута в минуту)")
    void shouldNotDetectOverlapWhenLessonsAreBackToBack() {
        var existingLesson = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime.minus(60, ChronoUnit.MINUTES))
                .endTime(baseTime)
                .status(LessonStatus.SCHEDULED)
                .build();
        entityManager.persistAndFlush(existingLesson);
        Instant newStart = baseTime;
        Instant newEnd = baseTime.plus(90, ChronoUnit.MINUTES);
        var result = lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(
                List.of(LessonStatus.SCHEDULED), newStart, newEnd
        );
        assertFalse(result);
    }

    @Test
    @DisplayName("Должен выбросить ConstraintViolationException при дублировании времени слота")
    void shouldThrowExceptionWhenDuplicateStartTime() {
        var firstLessonSingUp = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        var secondLessonSingUp = Lesson.builder()
                .studentId("student_2")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        entityManager.persistAndFlush(firstLessonSingUp);
        var exception = assertThrows(ConstraintViolationException.class, () ->
                entityManager.persistAndFlush(secondLessonSingUp)
        );
        var rootCause = exception.getCause();
        assertNotNull(rootCause);
        assertTrue(rootCause.getMessage().contains("unique_index_start_time"));
    }

    @Test
    @DisplayName("Должен успешно найти урок по статусу, ID и ID студента")
    void shouldFindLessonByStatusAndIdAndStudentId() {
        var existingLesson = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        var savedLesson = entityManager.persistAndFlush(existingLesson);
        var result = lessonRepository.findByStatusAndIdAndStudentId(
                LessonStatus.SCHEDULED,
                savedLesson.getId(),
                savedLesson.getStudentId()
        );
        assertTrue(result.isPresent());
        assertEquals(savedLesson.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Должен вернуть true, если у студента есть активный урок")
    void shouldReturnTrueWhenActiveLessonExistsForStudent() {
        var existingLesson = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(90, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        entityManager.persistAndFlush(existingLesson);
        var result = lessonRepository.existsByStatusInAndStudentId(
                List.of(LessonStatus.SCHEDULED),
                "student_1"
        );
        assertTrue(result);
    }

    @Test
    @DisplayName("Должен находить только SCHEDULED уроки в интервале времени с сортировкой по возрастанию")
    void shouldFindScheduledLessonsWithinDayRangeOrderedByStartTimeAsc() {
        var firstExistingLesson = Lesson.builder()
                .studentId("student_1")
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        var secondExistingLesson = Lesson.builder()
                .studentId("student_2")
                .startTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(LessonStatus.SCHEDULED)
                .build();
        entityManager.persistAndFlush(firstExistingLesson);
        entityManager.persistAndFlush(secondExistingLesson);
        var result = lessonRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
                List.of(LessonStatus.SCHEDULED),
                baseTime.truncatedTo(ChronoUnit.DAYS),
                baseTime.truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS)
        );
        assertEquals(List.of(firstExistingLesson, secondExistingLesson), result);
    }
}
