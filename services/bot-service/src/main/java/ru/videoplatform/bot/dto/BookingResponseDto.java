package ru.videoplatform.bot.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingResponseDto(UUID bookingId, Instant startTime, Instant endTime) { }
