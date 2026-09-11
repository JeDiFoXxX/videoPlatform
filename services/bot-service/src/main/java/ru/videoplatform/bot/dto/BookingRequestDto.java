package ru.videoplatform.bot.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingRequestDto(UUID studentId, String firstName, String lastName,
                                Instant startTime, Instant endTime) {

    public static BookingRequestDto from(StudentRequestDto request, Instant startTime, Instant endTime) {
        return new BookingRequestDto(request.studentId(), request.firstName(),
                request.lastName(), startTime, endTime
        );
    }
}
