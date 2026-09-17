package ru.videoplatform.bot.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingRequestDto(Long chatId, UUID studentId, String firstName, String lastName,
                                Instant startTime, Instant endTime) {

    public static BookingRequestDto from(StudentRequestDto request, Long chatId,
                                         Instant startTime, Instant endTime) {
        return new BookingRequestDto(
                chatId,
                request.studentId(),
                request.firstName(),
                request.lastName(),
                startTime,
                endTime
        );
    }
}
