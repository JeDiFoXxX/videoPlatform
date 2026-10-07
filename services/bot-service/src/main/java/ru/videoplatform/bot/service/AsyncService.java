package ru.videoplatform.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.videoplatform.bot.dto.NotificationBotRequestDto;

@Service
@RequiredArgsConstructor
public class AsyncService {

    private final TelegramClient telegramClient;

    @Async
    public void sendNotificationTelegramBot(NotificationBotRequestDto dto) {
        var message = SendMessage.builder()
                .chatId(dto.chatId())
                .text(dto.message())
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception ignored) { }
    }
}
