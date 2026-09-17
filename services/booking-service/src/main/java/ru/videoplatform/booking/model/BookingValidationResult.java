package ru.videoplatform.booking.model;

public record BookingValidationResult(int activeLessonsCount, int timeOverlapsCount) { }