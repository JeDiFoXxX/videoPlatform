package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingBotResponseDto(
        UUID bookingId,
        String firstName,
        String lastName,
        Instant startTime,
        Instant endTime,
        String status
) {

    public static BookingBotResponseDto from(Booking booking) {
        return new BookingBotResponseDto(
                booking.getId(),
                booking.getFirstName(),
                booking.getLastName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus().name()
        );
    }
}
