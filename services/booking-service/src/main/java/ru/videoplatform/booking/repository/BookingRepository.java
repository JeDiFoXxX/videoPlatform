package ru.videoplatform.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingValidationResult;
import ru.videoplatform.booking.model.BookingStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
            Collection<BookingStatus> statuses, Instant from, Instant to
    );

    List<Booking> findByStatusInAndStudentId(Collection<BookingStatus> statuses, UUID studentId);

    Optional<Booking> findByStatusAndId(BookingStatus status, UUID id);

    @Query(value = """
        SELECT 
            COUNT(CASE WHEN student_id = :studentId THEN 1 END)
            ::int AS activeBookingsCount,
            COUNT(CASE WHEN start_time < :endTime AND end_time > :startTime THEN 1 END)
            ::int AS timeOverlapsCount
        FROM booking
        WHERE status = 'SCHEDULED'
        """, nativeQuery = true)
    BookingValidationResult checkStudentBookingsAndIntersections(
            @Param("studentId") UUID studentId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}
