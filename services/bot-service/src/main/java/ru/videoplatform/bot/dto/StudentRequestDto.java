package ru.videoplatform.bot.dto;

import java.util.UUID;

public record StudentRequestDto(UUID studentId, String firstName, String lastName) { }
