package ru.videoplatform.signaling.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingCalendarResponseDto(UUID bookingId, String firstName, String lastName,
                                         Instant startTime, Instant endTime, String status) { }
