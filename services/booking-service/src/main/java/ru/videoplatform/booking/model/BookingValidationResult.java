package ru.videoplatform.booking.model;

public record BookingValidationResult(int activeBookingsCount, int timeOverlapsCount) { }