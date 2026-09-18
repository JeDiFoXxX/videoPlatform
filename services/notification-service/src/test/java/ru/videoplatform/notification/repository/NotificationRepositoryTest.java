package ru.videoplatform.notification.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.videoplatform.notification.model.Notification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(properties = {
        "spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:1111/test_notifications}",
        "services.bot-service.uri=${BOT_SERVICE_URI:http://localhost:2222}",
        "spring.data.mongodb.auto-index-creation=true",
})
@Testcontainers
class NotificationRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Должен находить уведомления по ID брони и удалять запись")
    void test() {
        var now = Instant.now();
        var notification = new Notification(null, now, 12345L, UUID.randomUUID().toString(),
                "test_first_name", now.plus(1, ChronoUnit.HOURS));

        notificationRepository.save(notification);
        notificationRepository.deleteByBookingId(notification.getBookingId());
        var emptyResult = notificationRepository.deleteByStartTime(now);

        assertTrue(emptyResult.isEmpty());
    }

    @Test
    @DisplayName("Должен находить уведомления по текущему времени и удалять запись")
    void shouldFindNotificationsWithRemindAtLessThanOrEqual() {
        var now = Instant.now();
        var notification = new Notification(null, now, 12345L, UUID.randomUUID().toString(),
                "test_first_name", now.plus(1, ChronoUnit.HOURS));

        notificationRepository.save(notification);
        var result = notificationRepository.deleteByStartTime(now);
        var emptyResult = notificationRepository.deleteByStartTime(now);

        assertTrue(result.isPresent());
        assertTrue(emptyResult.isEmpty());
        assertEquals("test_first_name", result.get().getFirstName());
    }
}