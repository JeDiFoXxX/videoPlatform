package ru.videoplatform.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.videoplatform.booking.client.NotificationClient;
import ru.videoplatform.booking.client.SignalingClient;
import ru.videoplatform.booking.dto.BookingCalendarResponseDto;
import ru.videoplatform.booking.dto.BookingNotificationRequestDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncService {

    private final NotificationClient notificationClient;
    private final SignalingClient signalingClient;

    @Async
    public void createNotificationAsync(String systemToken, BookingNotificationRequestDto requestDto) {
        try {
            notificationClient.createNotification(systemToken, requestDto);
        } catch (Exception ignored) { }
    }

    @Async
    public void deleteNotificationAsync(String systemToken, UUID bookingId) {
        try {
            notificationClient.deleteNotification(systemToken, bookingId);
        } catch (Exception ignored) { }
    }

    @Async
    public void createSignalingAsync(String systemToken, BookingCalendarResponseDto requestDto) {
        try {
            signalingClient.createSignaling(systemToken, requestDto);
        } catch (Exception ignored) { }
    }

    @Async
    public void deleteSignalingAsync(String systemToken, UUID bookingId) {
        try {
            signalingClient.deleteSignaling(systemToken, bookingId);
        } catch (Exception ignored) { }
    }
}
