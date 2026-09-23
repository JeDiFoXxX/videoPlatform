package ru.videoplatform.notification.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import ru.videoplatform.notification.model.Notification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///notification_db",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml",
        "services.bot-service.uri=http://localhost:1111"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Должен находить уведомления по ID брони и удалять запись")
    void test() {
        var startTime = Instant.now();
        var notification = createNotification(startTime);
        notificationRepository.save(notification);
        notificationRepository.deleteByBookingId(notification.getBookingId());
        var emptyResult = notificationRepository.deleteByStartTime(startTime);

        assertTrue(emptyResult.isEmpty());
    }

    @Test
    @DisplayName("Должен находить уведомления по текущему времени и удалять запись")
    void shouldFindNotificationsWithRemindAtLessThanOrEqual() {
        var startTime = Instant.now();
        var notification = createNotification(startTime);
        notificationRepository.save(notification);
        var result = notificationRepository.deleteByStartTime(startTime);
        var emptyResult = notificationRepository.deleteByStartTime(startTime);

        assertTrue(result.isPresent());
        assertTrue(emptyResult.isEmpty());
        assertEquals("test_first_name", result.get().getFirstName());
    }

    private Notification createNotification(Instant startTime) {
        return new Notification(
                null,
                12345L,
                UUID.randomUUID(),
                "test_first_name",
                startTime,
                startTime.plus(1, ChronoUnit.HOURS));
    }
}