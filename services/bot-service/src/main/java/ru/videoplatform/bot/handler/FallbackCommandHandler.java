package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

@Component
@Order
@RequiredArgsConstructor
public class FallbackCommandHandler implements CommandHandler {

    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return true;
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        return SendMessage.builder()
                .chatId(data.update().getCallbackQuery().getMessage().getChatId())
                .text("Выберите действие в меню снизу:")
                .replyMarkup(keyboardFactory.createMainMenuKeyboard())
                .build();
    }
}
