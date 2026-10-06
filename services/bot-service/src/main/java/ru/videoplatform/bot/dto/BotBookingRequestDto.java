package ru.videoplatform.bot.dto;

import java.time.Instant;
import java.util.UUID;

public record BotBookingRequestDto(Long chatId, UUID studentId, String firstName, String lastName,
                                   Instant startTime, Instant endTime) {

    public static BotBookingRequestDto from(StudentRequestDto request, Long chatId,
                                            Instant startTime, Instant endTime) {
        return new BotBookingRequestDto(
                chatId,
                request.studentId(),
                request.firstName(),
                request.lastName(),
                startTime,
                endTime
        );
    }
}
