package ru.videoplatform.bot.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.exception.BotTelegramException;

import java.util.UUID;

@Component
@Order(1)
@RequiredArgsConstructor
public class ConfirmDeleteBookingHandler implements CommandHandler {

    private final BookingClient bookingClient;

    @Override
    public boolean canHandle(String incomingText) {
        return incomingText != null && incomingText.startsWith(CALLBACK_CONFIRM_DELETE);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var message = data.update().getCallbackQuery().getMessage();
        try {
            bookingClient.deleteBooking(systemToken,
                    UUID.fromString(data.incomingText().substring(CALLBACK_CONFIRM_DELETE.length()))
            );
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text("✅ Ваш урок успешно отменен.")
                    .build();
        } catch (FeignException ex) {
            throw new BotTelegramException(ex.contentUTF8(),
                    message.getChatId(), message.getMessageId());
        }
    }
}
