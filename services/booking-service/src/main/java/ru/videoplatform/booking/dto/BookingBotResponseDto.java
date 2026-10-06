package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingBotResponseDto(UUID bookingId, Instant startTime, Instant endTime) {

    public static BookingBotResponseDto from(Booking booking) {
        return new BookingBotResponseDto(
                booking.getId(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}
