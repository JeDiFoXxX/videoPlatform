package ru.videoplatform.booking.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///videoplatform_booking",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml",
        "services.notification-service.uri=http://localhost:1111"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private final Instant baseTime = Instant.parse("2026-09-14T10:00:00Z");
    private final UUID studentId = UUID.randomUUID();

    @Test
    @DisplayName("Должен генерировать id при сохранении урока в БД")
    void shouldGenerateIdWhenBookingIsPersisted() {
        var bookingToSave = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        var savedBooking = entityManager.persistAndFlush(bookingToSave);

        assertNotNull(savedBooking.getId());
    }

    @Test
    @DisplayName("Должен обнаружить пересечение и посчитать активные уроки через нативный запрос")
    void shouldDetectOverlapAndCountActiveBookings() {
        var existingBooking = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();

        entityManager.persistAndFlush(existingBooking);

        var newStart = baseTime.plus(30, ChronoUnit.MINUTES);
        var newEnd = baseTime.plus(90, ChronoUnit.MINUTES);
        var result = bookingRepository.checkStudentBookingsAndIntersections(studentId, newStart, newEnd);

        assertNotNull(result);
        assertEquals(1, result.activeBookingsCount());
        assertEquals(1, result.timeOverlapsCount());
    }

    @Test
    @DisplayName("Не должен находить пересечение через нативный запрос, если уроки идут строго подряд")
    void shouldNotDetectOverlapWhenBookingsAreBackToBack() {
        var existingBooking = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime.minus(60, ChronoUnit.MINUTES))
                .endTime(baseTime)
                .status(BookingStatus.SCHEDULED)
                .build();

        entityManager.persistAndFlush(existingBooking);

        var newEnd = baseTime.plus(60, ChronoUnit.MINUTES);
        var result = bookingRepository.checkStudentBookingsAndIntersections(studentId, baseTime, newEnd);

        assertNotNull(result);
        assertEquals(1, result.activeBookingsCount());
        assertEquals(0, result.timeOverlapsCount());
    }

    @Test
    @DisplayName("Должен выбросить ConstraintViolationException при дублировании времени слота")
    void shouldThrowExceptionWhenDuplicateStartTime() {
        var firstBookingSignUp = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        var secondBookingSignUp = Booking.builder()
                .studentId(UUID.randomUUID())
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();

        entityManager.persistAndFlush(firstBookingSignUp);

        var exception = assertThrows(Exception.class, () ->
                entityManager.persistAndFlush(secondBookingSignUp)
        );

        assertTrue(exception.getCause() instanceof ConstraintViolationException
                || exception.getMessage().contains("unique_index_start_time"));
    }

    @Test
    @DisplayName("Должен успешно найти урок по статусу и ID для отмены")
    void shouldFindBookingByStatusAndId() {
        var existingBooking = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        var savedBooking = entityManager.persistAndFlush(existingBooking);

        var result = bookingRepository.findByStatusAndId(BookingStatus.SCHEDULED, savedBooking.getId());

        assertTrue(result.isPresent());
        assertEquals(savedBooking.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Должен находить запланированные уроки в интервале времени с сортировкой по возрастанию")
    void shouldFindScheduledBookingsWithinDayRangeOrderedByStartTimeAsc() {
        var firstExistingBooking = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        var secondExistingBooking = Booking.builder()
                .studentId(UUID.randomUUID())
                .startTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .endTime(baseTime.plus(120, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();

        entityManager.persistAndFlush(firstExistingBooking);
        entityManager.persistAndFlush(secondExistingBooking);

        var result = bookingRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
                List.of(BookingStatus.SCHEDULED),
                baseTime.truncatedTo(ChronoUnit.DAYS),
                baseTime.truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS)
        );

        assertEquals(2, result.size());
        assertEquals(firstExistingBooking.getId(), result.getFirst().getId());
        assertEquals(secondExistingBooking.getId(), result.getLast().getId());
    }

    @Test
    @DisplayName("Должен находить все активные уроки конкретного студента")
    void shouldFindAllActiveBookingsForSpecificStudent() {
        var studentBooking = Booking.builder()
                .studentId(studentId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        var anotherStudentBooking = Booking.builder()
                .studentId(UUID.randomUUID())
                .startTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .endTime(baseTime.plus(120, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();

        entityManager.persist(studentBooking);
        entityManager.persist(anotherStudentBooking);
        entityManager.flush();

        var result = bookingRepository.findByStatusInAndStudentId(
                List.of(BookingStatus.SCHEDULED),
                studentId
        );

        assertEquals(1, result.size());
        assertEquals(studentBooking.getId(), result.getFirst().getId());
    }
}
