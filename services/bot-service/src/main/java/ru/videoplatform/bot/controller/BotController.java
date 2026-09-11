package ru.videoplatform.bot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.service.BotService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;
    private final ObjectMapper telegramObjectMapper;

    @PostMapping("/event")
    public ResponseEntity<?> processTelegramUpdate(
            @RequestHeader("Authorization") String systemToken,
            @RequestHeader("Student-id") String studentId,
            @RequestHeader("First-name") String firstName,
            @RequestHeader("Last-name") String lastName,
            @RequestBody String message) throws JsonProcessingException {

        return Optional.ofNullable(botService.processTelegramEvent(
                        systemToken,
                        telegramObjectMapper.readValue(message, Update.class),
                        new StudentRequestDto(UUID.fromString(studentId), firstName, lastName)
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().build());
    }
}
