package ru.videoplatform.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.videoplatform.bot.dto.StudentRequestDto;
import ru.videoplatform.bot.handler.CommandHandler;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @Mock
    private CommandHandler messageHandler;

    @Mock
    private CommandHandler callbackHandler;

    private BotService botService;

    @BeforeEach
    void setUp() {
        botService = new BotService(List.of(messageHandler, callbackHandler));
    }

    @Test
    @DisplayName("Должен успешно определить текстовое сообщение и передать нужному хэндлеру")
    void shouldRouteTextMessageToCorrectHandler() {
        var chatId = 12345L;
        var message = "/start";
        var update = createMessageUpdate(chatId, message);
        var expectedResponse = new SendMessage(String.valueOf(chatId), "messageHandlerFinished");

        given(messageHandler.canHandle(message)).willReturn(true);
        given(messageHandler.handle(any(), any(), any())).willAnswer(invocation -> expectedResponse);

        var serviceResponse = botService.processTelegramEvent(
                "system_token",
                update,
                createStudentDto());

        assertNotNull(serviceResponse);
        assertEquals(expectedResponse, serviceResponse);
        verify(messageHandler, times(1)).handle(any(), any(), any());
        verify(callbackHandler, never()).handle(any(), any(), any());
    }

    @Test
    @DisplayName("Должен успешно определить нажатие кнопки и передать нужному хэндлеру")
    void shouldRouteCallbackQueryToCorrectHandler() {
        var chatId = 12345L;
        var message = "/start";
        var callbackData = "/callbackQuery";
        var update = createCallbackQueryUpdate(chatId, message, callbackData);
        var expectedResponse = new SendMessage(String.valueOf(chatId), "callbackHandlerFinished");

        given(callbackHandler.canHandle(callbackData)).willReturn(true);
        given(callbackHandler.handle(any(), any(), any())).willAnswer(invocation -> expectedResponse);

        var serviceResponse = botService.processTelegramEvent(
                "system_token",
                update,
                createStudentDto());

        assertNotNull(serviceResponse);
        assertEquals(expectedResponse, serviceResponse);
        verify(messageHandler, never()).handle(any(), any(), any());
        verify(callbackHandler, times(1)).handle(any(), any(), any());
    }

    @Test
    @DisplayName("Должен вернуть null, если пришел пустой Update")
    void shouldReturnNullWhenUpdateIsInvalid() {
        var serviceResponse = botService.processTelegramEvent(
                "system_token",
                new Update(),
                createStudentDto());

        assertNull(serviceResponse);
        verify(messageHandler, never()).canHandle(any());
        verify(callbackHandler, never()).canHandle(any());
    }

    private StudentRequestDto createStudentDto() {
        return new StudentRequestDto(
                UUID.randomUUID(),
                "test_first_name",
                "test_last_name"
        );
    }

    private Update createMessageUpdate(Long chatId, String text) {
        var chat = Chat.builder()
                .id(chatId)
                .type("private")
                .build();
        var message = Message.builder()
                .text(text)
                .chat(chat)
                .build();
        var update = new Update();
        update.setMessage(message);
        return update;
    }

    private Update createCallbackQueryUpdate(Long chatId, String text, String callbackData) {
        var chat = Chat.builder()
                .id(chatId)
                .type("private")
                .build();
        var message = Message.builder()
                .text(text)
                .chat(chat)
                .build();
        var callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(message);
        callbackQuery.setData(callbackData);
        var update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }
}