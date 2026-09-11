package ru.videoplatform.bot.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.videoplatform.bot.dto.BookingResponseDto;
import ru.videoplatform.bot.handler.CommandHandler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ru.videoplatform.bot.handler.CommandHandler.*;

@Component
public class KeyboardFactory {

    private final ZoneId moscowZone = ZoneId.of("Europe/Moscow");

    private final DateTimeFormatter dayFormatter = DateTimeFormatter
            .ofPattern("E d MMMM", Locale.of("ru"))
            .withZone(moscowZone);

    private final DateTimeFormatter timeFormatter = DateTimeFormatter
            .ofPattern("HH:mm").withZone(ZoneOffset.UTC)
            .withZone(moscowZone);

    public ReplyKeyboardMarkup createMainMenuKeyboard() {
        var bookingButton = KeyboardButton.builder()
                .text(MENU_BOOKING_START)
                .build();

        var cancelButton = KeyboardButton.builder()
                .text(CommandHandler.MENU_BOOKING_DELETE)
                .build();

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(new KeyboardRow(bookingButton, cancelButton)))
                .resizeKeyboard(true)
                .isPersistent(true)
                .oneTimeKeyboard(false)
                .build();
    }

    public InlineKeyboardMarkup createDaysInlineKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        var builder = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            var futureDate = Instant.now().plus(i, ChronoUnit.DAYS);
            var zeroTimeUtc = futureDate.atZone(moscowZone)
                    .toLocalDate()
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
            var dateParts = dayFormatter.format(futureDate).split(" ");
            builder.setLength(0);
            builder.append(dateParts[0].toUpperCase())
                    .append(" ")
                    .append(dateParts[1])
                    .append(" ")
                    .append(dateParts[2].substring(0, 1).toUpperCase())
                    .append(dateParts[2].substring(1));
            var button = InlineKeyboardButton.builder()
                    .text(builder.toString())
                    .callbackData(CALLBACK_SELECT_DAY + zeroTimeUtc.toString())
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }
        var backButton = InlineKeyboardButton.builder()
                .text("⬅️ В главное меню")
                .callbackData(CALLBACK_CLOSE_MENU)
                .build();
        rows.add(new InlineKeyboardRow(backButton));
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup createAvailableSlotsKeyboard(List<Instant> availableSlots) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Instant slot : availableSlots) {
            var buttonText = timeFormatter.format(slot);
            var callbackData = String.format("%s:%s:%s",
                    CALLBACK_SELECT_TIME,
                    slot,
                    slot.plus(1, ChronoUnit.HOURS)
            );
            var button = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData(callbackData)
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }
        var backButton = InlineKeyboardButton.builder()
                .text("⬅️ Назад к выбору даты")
                .callbackData(MENU_BOOKING_START)
                .build();
        rows.add(new InlineKeyboardRow(backButton));
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup createActiveBookingKeyboard(List<BookingResponseDto> activeBooking) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        var builder = new StringBuilder();
        for (BookingResponseDto booking : activeBooking) {
            var dateParts = dayFormatter.format(booking.startTime()).split(" ");
            builder.setLength(0);
            builder.append(dateParts[0].toUpperCase())
                    .append(" ")
                    .append(dateParts[1])
                    .append(" ")
                    .append(dateParts[2].substring(0, 1).toUpperCase())
                    .append(dateParts[2].substring(1))
                    .append(" ")
                    .append(timeFormatter.format(booking.startTime()))
                    .append(" - ")
                    .append(timeFormatter.format(booking.endTime()));
            var button = InlineKeyboardButton.builder()
                    .text(builder.toString())
                    .callbackData(CALLBACK_CONFIRM_DELETE + booking.bookingId())
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }
        var backButton = InlineKeyboardButton.builder()
                .text("⬅️ В главное меню")
                .callbackData(CALLBACK_CLOSE_MENU)
                .build();
        rows.add(new InlineKeyboardRow(backButton));
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
