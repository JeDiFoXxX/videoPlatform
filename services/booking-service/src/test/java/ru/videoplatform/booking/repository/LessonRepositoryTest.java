package ru.videoplatform.booking.repository;

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
        var result = lessonRepository.existsByStatusInAndStartTimeBeforeAndEndTimeAfter(
                List.of(LessonStatus.SCHEDULED), newEnd, newStart
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
        var result = lessonRepository.existsByStatusInAndStartTimeBeforeAndEndTimeAfter(
                List.of(LessonStatus.SCHEDULED), newEnd, newStart
        );
        assertFalse(result);
    }
}