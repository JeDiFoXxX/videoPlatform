package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Lesson;
import ru.videoplatform.booking.model.LessonStatus;

import java.time.Instant;

public record LessonCreateDto(String studentId, Instant startTime, Instant endTime) {

    public Lesson toEntity() {
        return Lesson.builder()
                .studentId(studentId())
                .startTime(startTime())
                .endTime(endTime())
                .status(LessonStatus.SCHEDULED)
                .build();
    }
}
