package ru.videoplatform.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.videoplatform.bot.dto.StudentRequestDto;

@Component
@Order(1)
@RequiredArgsConstructor
public class SelectCloseMenuHandler implements CommandHandler {

    @Override
    public boolean canHandle(String incomingText) {
        return CALLBACK_CLOSE_MENU.equals(incomingText);
    }

    @Override
    public BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto) {
        var message = data.update().getCallbackQuery().getMessage();
        return DeleteMessage.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .build();
    }
}
