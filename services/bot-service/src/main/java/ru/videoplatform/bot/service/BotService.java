package ru.videoplatform.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.handler.CommandHandler;

import java.util.List;

import static ru.videoplatform.bot.handler.CommandHandler.TelegramData;

@Service
@RequiredArgsConstructor
public class BotService {

    private final List<CommandHandler> allHandlers;

    public BotApiMethod<?> processTelegramEvent(String systemToken, Update update, StudentRequestDto dto) {
        var telegramData = parseTelegramData(update);
        if (telegramData == null) {
            return null;
        }

        return allHandlers.stream()
                .filter(handler -> handler.canHandle(telegramData.incomingText()))
                .findFirst()
                .map(handler -> handler.handle(
                        systemToken,
                        parseTelegramData(update),
                        dto))
                .orElse(null);
    }

    private TelegramData parseTelegramData(Update update) {
        if (update.hasMessage() && update.getMessage().getChatId() != null) {
            return new TelegramData(update.getMessage().getText(), update);
        }

        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage() != null) {
            return new TelegramData(update.getCallbackQuery().getData(), update);
        }

        return null;
    }
}
