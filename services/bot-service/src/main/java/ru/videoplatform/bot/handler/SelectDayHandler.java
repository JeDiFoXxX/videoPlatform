package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

import java.time.Instant;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectDayHandler implements CommandHandler {

    private final BookingClient bookingClient;
    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return incomingText != null && incomingText.startsWith(CALLBACK_SELECT_DAY);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var availableSlots = bookingClient.getAvailableSlots(systemToken,
                Instant.parse(data.incomingText().substring(CALLBACK_SELECT_DAY.length())));
        var message = data.update().getCallbackQuery().getMessage();

        if (availableSlots.isEmpty()) {
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text("⚠️ Нет мест на этот день. Выберите другую дату:")
                    .replyMarkup(keyboardFactory.createDaysInlineKeyboard())
                    .build();
        }

        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("⏰ Выберите время записи по МСК:")
                .replyMarkup(keyboardFactory.createAvailableSlotsKeyboard(availableSlots))
                .build();
    }
}
