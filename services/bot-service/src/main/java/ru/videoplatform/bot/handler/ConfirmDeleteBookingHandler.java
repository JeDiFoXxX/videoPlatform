package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
public class ConfirmDeleteBookingHandler implements CommandHandler {

    private final BookingClient bookingClient;
    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return incomingText != null && incomingText.startsWith(CALLBACK_CONFIRM_DELETE);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var isDeleted = bookingClient.deleteBooking(systemToken,
                UUID.fromString(data.incomingText().substring(CALLBACK_CONFIRM_DELETE.length())));
        var message = data.update().getCallbackQuery().getMessage();
        if (!isDeleted) {
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text("⚠️ Не удалось отменить запись. Похоже, она уже была удалена ранее.")
                    .build();
        }

        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("✅ Ваш урок успешно отменен.")
                .build();
    }
}
