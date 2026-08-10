package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Lesson;

import java.time.Instant;
import java.util.UUID;

public record LessonResponseDto(UUID id, String studentId,
                                Instant startTime, Instant endTime, String status) {

    public static LessonResponseDto from(Lesson lesson) {
        return new LessonResponseDto(
                lesson.getId(),
                lesson.getStudentId(),
                lesson.getStartTime(),
                lesson.getEndTime(),
                lesson.getStatus().name()
        );
    }
}