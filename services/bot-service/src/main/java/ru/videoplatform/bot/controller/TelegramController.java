package ru.videoplatform.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.videoplatform.bot.dto.UserRequestDto;
import ru.videoplatform.bot.service.BotService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
public class TelegramController {

    private final BotService botService;
    private final ObjectMapper telegramObjectMapper;

    @PostMapping("/event")
    public ResponseEntity<?> receiveUpdate(
            @RequestBody String rawJson,
            @RequestHeader("User-id") String userId,
            @RequestHeader("First-name") String firstName,
            @RequestHeader("Last-name") String lastName) throws Exception {

        var reply = botService.event(
                telegramObjectMapper.readValue(rawJson, Update.class),
                new UserRequestDto(userId, firstName, lastName)
        );
        return ResponseEntity.ok(Objects.requireNonNullElse(reply, "ok"));
    }
}
