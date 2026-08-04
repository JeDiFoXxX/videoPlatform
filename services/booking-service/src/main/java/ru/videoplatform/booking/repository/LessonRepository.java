package ru.videoplatform.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.videoplatform.booking.model.Lesson;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByStartTimeBetweenOrderByStartTimeAsc(Instant from, Instant to);

    List<Lesson> findByStartTimeBetween(Instant monthStart, Instant monthEnd);

    boolean existsByStartTimeBeforeAndEndTimeAfter(Instant requestedEnd, Instant requestedStart);
}
