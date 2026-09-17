package ru.videoplatform.bot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class BotGlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(BotTelegramException.class)
    public ResponseEntity<?> handleBotTelegramException(BotTelegramException ex) {
        var exMessage = "Произошла ошибка. Попробуйте позже.";
        try {
            var jsonNode = objectMapper.readTree(ex.getMessage());
            if (jsonNode != null && jsonNode.has("message")) {
                exMessage = jsonNode.get("message").asString();
            }
        } catch (Exception ignore) {
            log.debug("Не удалось распарсить JSON ошибки Telegram");
        }

        var editMessage = EditMessageText.builder()
                .chatId(ex.getChatId())
                .messageId(ex.getMessageId())
                .text("⚠️ " + exMessage)
                .build();

        return ResponseEntity.ok(editMessage);
    }
}
