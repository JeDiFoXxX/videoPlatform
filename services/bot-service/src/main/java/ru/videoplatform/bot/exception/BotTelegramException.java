package ru.videoplatform.bot.exception;

import lombok.Getter;

@Getter
public class BotTelegramException extends RuntimeException {
    public final long chatId;
    public final int messageId;

    public BotTelegramException(String message, long chatId, int messageId) {
        super(message);
        this.chatId = chatId;
        this.messageId = messageId;
    }
}
