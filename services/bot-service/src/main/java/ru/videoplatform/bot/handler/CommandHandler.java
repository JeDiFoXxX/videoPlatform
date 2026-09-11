package ru.videoplatform.bot.handler;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.videoplatform.bot.dto.StudentRequestDto;

public interface CommandHandler {

    String MENU_BOOKING_START = "📅 Запись на урок";
    String MENU_BOOKING_DELETE = "❌ Удалить запись";

    String CALLBACK_SELECT_DAY = "select_day:";
    String CALLBACK_SELECT_TIME = "select_time:";
    String CALLBACK_CONFIRM_DELETE = "confirm_delete:";

    String CALLBACK_CLOSE_MENU = "close_menu";

    boolean canHandle(String incomingText);

    BotApiMethod<?> handle(String systemToken, TelegramData data, StudentRequestDto dto);

    record TelegramData(String incomingText, Update update) { }
}
