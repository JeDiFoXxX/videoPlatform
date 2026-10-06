package ru.videoplatform.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingNotificationRequestDto(Long chatId, UUID bookingId, String firstName,
                                            Instant startTime, Instant endTime) {

    public static BookingNotificationRequestDto from(BotBookingRequestDto requestDto, UUID bookingId) {
        return new BookingNotificationRequestDto(
                requestDto.chatId(),
                bookingId,
                requestDto.firstName(),
                requestDto.startTime(),
                requestDto.endTime()
        );
    }
}
