package ru.videoplatform.booking.dto;

import ru.videoplatform.booking.model.BookingStatus;

import java.time.Instant;
import java.util.List;

public record BookingCalendarRequestDto(List<BookingStatus> statuses, Instant from, Instant to) { }
