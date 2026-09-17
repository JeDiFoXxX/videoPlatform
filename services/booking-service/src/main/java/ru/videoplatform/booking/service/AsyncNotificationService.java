package ru.videoplatform.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.videoplatform.booking.client.NotificationClient;
import ru.videoplatform.booking.dto.NotificationRequestDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationService {

    private final NotificationClient notificationClient;

    @Async
    public void createNotificationAsync(String systemToken, NotificationRequestDto requestDto) {
        try {
            notificationClient.createNotification(systemToken, requestDto);
        } catch (Exception ignore) {
            log.debug("Ошибка создания уведомления");
        }
    }

    @Async
    public void deleteNotificationAsync(String systemToken, UUID bookingId) {
        try {
            notificationClient.deleteNotification(systemToken, bookingId);
        } catch (Exception ignore) {
            log.debug("Ошибка удаления уведомления");
        }
    }
}
