package ru.videoplatform.bot.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.videoplatform.bot.dto.BookingResponseDto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardFactoryTest {

    private final KeyboardFactory keyboardFactory = new KeyboardFactory();

    private static final String MENU_BOOKING_START = "📅 Запись на урок";
    private static final String MENU_BOOKING_DELETE = "❌ Удалить запись";
    private static final String CALLBACK_SELECT_DAY = "select_day:";
    private static final String CALLBACK_SELECT_TIME = "select_time:";
    private static final String CALLBACK_CONFIRM_DELETE = "confirm_delete:";
    private static final String CALLBACK_CLOSE_MENU = "close_menu";

    @Test
    @DisplayName("Должен содержать 2 кнопки главного меню: Запись на урок и Удалить запись")
    void shouldCreateCorrectMainMenuKeyboard() {
        var keyboard = keyboardFactory.createMainMenuKeyboard();
        var rows = keyboard.getKeyboard();
        var firstRow = rows.getFirst();

        assertNotNull(keyboard);
        assertTrue(keyboard.getResizeKeyboard());
        assertTrue(keyboard.getIsPersistent());
        assertFalse(keyboard.getOneTimeKeyboard());
        assertEquals(1, rows.size());
        assertEquals(MENU_BOOKING_START, firstRow.getFirst().getText());
        assertEquals(MENU_BOOKING_DELETE, firstRow.getLast().getText());
    }

    @Test
    @DisplayName("Должна содержать 7 дней с префиксом 'select_day:' и 1 кнопку 'В главное меню'")
    void shouldCreateDaysInlineKeyboardWithSevenDaysAndBackButton() {
        var keyboard = keyboardFactory.createDaysInlineKeyboard();
        var rows = keyboard.getKeyboard();
        var firstDayBtn = rows.getFirst().getFirst();
        var backBtn = rows.getLast().getFirst();

        assertNotNull(keyboard);
        assertEquals(8, rows.size());
        assertEquals("⬅️ В главное меню", backBtn.getText());
        assertEquals(CALLBACK_CLOSE_MENU, backBtn.getCallbackData());
        assertTrue(firstDayBtn.getCallbackData().startsWith(CALLBACK_SELECT_DAY));
    }

    @Test
    @DisplayName("Должен корректно формировать время с префиксом select_time:")
    void shouldCreateAvailableSlotsKeyboardWithCorrectCallbackData() {
        var availableSlot = Instant.parse("2026-10-10T10:00:00Z");
        var keyboard = keyboardFactory.createAvailableSlotsKeyboard(List.of(availableSlot));
        var rows = keyboard.getKeyboard();
        var availableSlotCallbackData = rows.getFirst().getFirst().getCallbackData();
        var backBtn = rows.getLast().getFirst();
        var expectedCallback = String.format("%s:%s:%s",
                CALLBACK_SELECT_TIME,
                availableSlot,
                availableSlot.plus(1, ChronoUnit.HOURS));

        assertNotNull(keyboard);
        assertEquals(2, rows.size());
        assertEquals(expectedCallback, availableSlotCallbackData);
        assertEquals("⬅️ Назад к выбору даты", backBtn.getText());
        assertEquals(MENU_BOOKING_START, backBtn.getCallbackData());
    }

    @Test
    @DisplayName("Должен выводить ID брони с префиксом confirm_delete:")
    void shouldCreateActiveBookingKeyboardWithBookingId() {
        var bookingId = UUID.randomUUID();
        var booking = new BookingResponseDto(
                bookingId,
                Instant.parse("2026-10-15T14:00:00Z"),
                Instant.parse("2026-10-15T15:00:00Z"));
        var keyboard = keyboardFactory.createActiveBookingKeyboard(List.of(booking));
        var rows = keyboard.getKeyboard();
        var bookingBtn = rows.getFirst().getFirst();
        var backBtn = rows.getLast().getFirst();

        assertNotNull(keyboard);
        assertEquals(2, rows.size());
        assertEquals(CALLBACK_CONFIRM_DELETE + bookingId, bookingBtn.getCallbackData());
        assertEquals("⬅️ В главное меню", backBtn.getText());
        assertEquals(CALLBACK_CLOSE_MENU, backBtn.getCallbackData());
    }
}