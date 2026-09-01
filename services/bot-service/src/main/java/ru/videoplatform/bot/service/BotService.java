package ru.videoplatform.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.videoplatform.bot.dto.UserRequestDto;

@Service
@RequiredArgsConstructor
public class BotService {

    public BotApiMethod<?> event(Update update, UserRequestDto dto) {
        var chatId = update.getMessage().getChatId();
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("Запрос дошёл до BotService!")
                .build();
    }
}
