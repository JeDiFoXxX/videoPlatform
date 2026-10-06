package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BotBookingRequestDto(Long chatId, UUID studentId, String firstName,
                                   String lastName, Instant startTime, Instant endTime) {

    public Booking toEntity() {
        return Booking.builder()
                .studentId(studentId())
                .firstName(firstName)
                .lastName(lastName)
                .startTime(startTime())
                .endTime(endTime())
                .status(BookingStatus.SCHEDULED)
                .build();
    }
}
