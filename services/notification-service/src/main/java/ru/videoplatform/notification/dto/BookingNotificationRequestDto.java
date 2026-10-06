package ru.videoplatform.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingNotificationRequestDto(Long chatId, UUID bookingId, String firstName,
                                            Instant startTime, Instant endTime) { }
