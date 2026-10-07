package ru.videoplatform.signaling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.videoplatform.signaling.dto.BookingCalendarResponseDto;
import ru.videoplatform.signaling.storage.SessionStorage;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncService {

    private final WebSocketMessageCodec messageCodec;
    private final SessionStorage sessionStorage;

    @Async
    public void sendCreateEvent(BookingCalendarResponseDto responseDto) {
        var teacherSession = sessionStorage.getTeacherSession();
        if (teacherSession != null && teacherSession.isOpen()) {
            var message = messageCodec.createMessage("booking_created", responseDto);
            try {
                teacherSession.sendMessage(message);
            } catch (Exception ignore) { }
        }
    }

    @Async
    public void sendDeleteEvent(UUID bookingId) {
        var teacherSession = sessionStorage.getTeacherSession();
        if (teacherSession != null && teacherSession.isOpen()) {
            var message = messageCodec.createMessage("booking_deleted", bookingId);
            try {
                teacherSession.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
