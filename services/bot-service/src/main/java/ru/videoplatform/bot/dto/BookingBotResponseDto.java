package ru.videoplatform.bot.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingBotResponseDto(UUID bookingId, Instant startTime, Instant endTime) { }
