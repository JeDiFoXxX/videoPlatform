package ru.videoplatform.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.videoplatform.bot.dto.BotNotificationRequestDto;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.handler.CommandHandler;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.videoplatform.bot.handler.CommandHandler.TelegramData;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {

    private final StringRedisTemplate stringRedisTemplate;
    private final TelegramClient telegramClient;
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

    @Async
    public void sendNotificationTelegramBot(BotNotificationRequestDto dto) {
        var message = SendMessage.builder()
                .chatId(dto.chatId())
                .text(dto.message())
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.debug("Ошибка отправки уведомления в Telegram");
        }
    }

    public Map<String, String> createKeyForQrCode(String uuid) {
        var key = UUID.randomUUID().toString().split("-")[0];
        stringRedisTemplate.opsForValue().set(key, uuid, Duration.ofMinutes(5));
        return Map.of("key", key);
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
