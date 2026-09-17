package ru.videoplatform.bot.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.exception.BotTelegramException;
import ru.videoplatform.bot.factory.KeyboardFactory;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectDeleteBookingHandler implements CommandHandler {

    private final BookingClient bookingClient;
    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return MENU_BOOKING_DELETE.equals(incomingText);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var message = data.update().getMessage();
        try {
            var activeBooking = bookingClient.getActiveBooking(systemToken, dto.studentId());

            if (activeBooking.isEmpty()) {
                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("⚠️ У вас нет активных уроков.")
                        .build();
            }

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("📋 Выберите занятие для отмены:")
                    .replyMarkup(keyboardFactory.createActiveBookingKeyboard(activeBooking))
                    .build();

        } catch (FeignException ex) {
            throw new BotTelegramException(ex.contentUTF8(),
                    message.getChatId(), message.getMessageId());
        }
    }
}
