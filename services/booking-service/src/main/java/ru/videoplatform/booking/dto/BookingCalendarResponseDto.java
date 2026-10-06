package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingStatus;

import java.time.Instant;

public record BookingCalendarResponseDto(String firstName, String lastName,
                                         Instant startTime, Instant endTime, BookingStatus status) {

    public static BookingCalendarResponseDto from(Booking booking) {
        return new BookingCalendarResponseDto(
                booking.getFirstName(),
                booking.getLastName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus()
        );
    }
}
