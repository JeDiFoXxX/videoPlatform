package ru.videoplatform.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.videoplatform.notification.dto.NotificationRequestDto;
import ru.videoplatform.notification.model.Notification;
import ru.videoplatform.notification.repository.NotificationRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(NotificationRequestDto dto) {
        notificationRepository.save(Notification.from(dto));
    }

    @Transactional
    public void deleteNotification(UUID bookingId) {
        notificationRepository.deleteByBookingId(bookingId);
    }

    @Transactional
    public Optional<Notification> getAndDeleteNotifications(Instant startTime) {
        return notificationRepository.deleteByStartTime(startTime);
    }

}
