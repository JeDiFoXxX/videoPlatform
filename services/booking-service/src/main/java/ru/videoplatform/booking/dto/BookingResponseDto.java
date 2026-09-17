package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingResponseDto(UUID bookingId, Instant startTime, Instant endTime) {

    public static BookingResponseDto from(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}
