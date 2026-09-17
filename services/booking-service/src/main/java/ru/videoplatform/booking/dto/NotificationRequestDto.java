package ru.videoplatform.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationRequestDto(Long chatId, UUID bookingId, String firstName,
                                     Instant startTime, Instant endTime) {

    public static NotificationRequestDto from(BookingRequestDto requestDto, UUID bookingId) {
        return new NotificationRequestDto(
                requestDto.chatId(),
                bookingId,
                requestDto.firstName(),
                requestDto.startTime(),
                requestDto.endTime()
        );
    }
}
