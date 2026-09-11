package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.StudentRequestDto;
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
        var activeBooking = bookingClient.getActiveBooking(systemToken, dto.studentId());
        var message = data.update().getMessage();

        if (activeBooking.isEmpty()) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("⚠️ У вас нет активных уроков.")
                    .build();
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("\uD83D\uDCCB Выберите занятие для отмены:")
                .replyMarkup(keyboardFactory.createActiveBookingKeyboard(activeBooking))
                .build();

    }
}
