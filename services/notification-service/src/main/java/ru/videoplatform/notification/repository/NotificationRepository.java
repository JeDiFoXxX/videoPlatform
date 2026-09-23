package ru.videoplatform.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.videoplatform.notification.model.Notification;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Optional<Notification> deleteByStartTime(Instant startTime);

    void deleteByBookingId(UUID bookingId);
}
