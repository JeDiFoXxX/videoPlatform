package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingCalendarResponseDto(UUID bookingId, String firstName, String lastName,
                                         Instant startTime, Instant endTime, String status) {

    public static BookingCalendarResponseDto from(Booking booking) {
        return new BookingCalendarResponseDto(
                booking.getId(),
                booking.getFirstName(),
                booking.getLastName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus().name()
        );
    }

    public static BookingCalendarResponseDto from(BookingBotResponseDto responseDto) {
        return new BookingCalendarResponseDto(
                responseDto.bookingId(),
                responseDto.firstName(),
                responseDto.lastName(),
                responseDto.startTime(),
                responseDto.endTime(),
                responseDto.status()
        );
    }
}
