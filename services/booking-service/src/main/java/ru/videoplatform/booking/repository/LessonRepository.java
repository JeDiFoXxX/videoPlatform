package ru.videoplatform.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.videoplatform.booking.model.Lesson;
import ru.videoplatform.booking.model.LessonStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
            Collection<LessonStatus> statuses, Instant from, Instant to
    );

    boolean existsByStatusInAndEndTimeAfterAndStartTimeBefore(
            Collection<LessonStatus> statuses, Instant startTime, Instant endTime
    );

    boolean existsByStatusInAndStudentId(Collection<LessonStatus> statuses, String studentId);
}
