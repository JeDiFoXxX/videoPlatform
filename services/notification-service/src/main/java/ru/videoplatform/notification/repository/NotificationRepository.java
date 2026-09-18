package ru.videoplatform.notification.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.videoplatform.notification.model.Notification;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Optional<Notification> deleteByStartTime(Instant startTime);

    void deleteByBookingId(String bookingId);
}
