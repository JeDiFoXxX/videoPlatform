package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectBookingHandler implements CommandHandler {

    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return MENU_BOOKING_START.equals(incomingText);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var text = "📅 Выберите дату для записи:";
        var daysKeyboard = keyboardFactory.createDaysInlineKeyboard();
        var message = data.update().hasCallbackQuery()
                ? data.update().getCallbackQuery().getMessage()
                : data.update().getMessage();

        if (data.update().hasCallbackQuery()) {
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text(text)
                    .replyMarkup(daysKeyboard)
                    .build();
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(text)
                .replyMarkup(daysKeyboard)
                .build();
    }
}
