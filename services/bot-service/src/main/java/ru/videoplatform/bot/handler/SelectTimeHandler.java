package ru.videoplatform.bot.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.videoplatform.bot.client.BookingClient;
import ru.videoplatform.bot.dto.BookingRequestDto;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.exception.BotTelegramException;

import java.time.Instant;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectTimeHandler implements CommandHandler {

    private final BookingClient bookingClient;

    @Override
    public boolean canHandle(String incomingText) {
        return incomingText != null && incomingText.startsWith(CALLBACK_SELECT_TIME);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var timeParts = data.incomingText().substring(CALLBACK_SELECT_TIME.length()).split("#");
        var message = data.update().getCallbackQuery().getMessage();
        try {
            bookingClient.createBooking(systemToken,
                    BookingRequestDto.from(
                            dto,
                            message.getChatId(),
                            Instant.parse(timeParts[0]),
                            Instant.parse(timeParts[1])));
            return EditMessageText.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .text("✅ Вы успешно записались на занятие!")
                    .build();
        } catch (FeignException ex) {
            throw new BotTelegramException(ex.contentUTF8(),
                    message.getChatId(), message.getMessageId());
        }
    }
}
