package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

@Component
@Order(1)
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return "/start".equals(incomingText);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var welcomeText = String.format("Добро пожаловать, %s!", dto.firstName());

        return SendMessage.builder()
                .chatId(data.update().getMessage().getChatId())
                .text(welcomeText)
                .replyMarkup(keyboardFactory.createMainMenuKeyboard())
                .build();
    }
}
