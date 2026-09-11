package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.BookingRequestDto;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.factory.KeyboardFactory;

import java.time.Instant;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectTimeHandler implements CommandHandler {

    private final BookingClient bookingClient;
    private final KeyboardFactory keyboardFactory;

    @Override
    public boolean canHandle(String incomingText) {
        return incomingText != null && incomingText.startsWith(CALLBACK_SELECT_TIME);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var timeParts = data.incomingText().split(":");
        var message = data.update().getCallbackQuery().getMessage();
        var isCreated = bookingClient.createBooking(systemToken,
                BookingRequestDto.from(dto, Instant.parse(timeParts[1]), Instant.parse(timeParts[2])));

        if (!isCreated) {
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text("⚠️ К сожалению, это время уже занято.")
                    .build();
        }

        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("✅ Вы успешно записались на занятие!")
                .build();
    }
}
