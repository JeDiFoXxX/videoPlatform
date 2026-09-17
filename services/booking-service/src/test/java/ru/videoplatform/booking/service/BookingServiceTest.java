package ru.videoplatform.booking.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.videoplatform.booking.dto.BookingRequestDto;
import ru.videoplatform.booking.exception.SlotConflictException;
import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingStatus;
import ru.videoplatform.booking.model.BookingValidationResult;
import ru.videoplatform.booking.repository.BookingRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequestDto validDto;
    private Instant baseTime;
    private UUID studentId;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        baseTime = Instant.now().truncatedTo(ChronoUnit.HOURS);

        validDto = new BookingRequestDto(
                12345L,
                studentId,
                "test_first_name",
                "test_last_name",
                baseTime,
                baseTime.plus(60, ChronoUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Должен успешно создавать запись, когда лимиты и пересечения чисты")
    void shouldCreateBookingSuccessfully() {
        var validation = new BookingValidationResult(0, 0);
        given(bookingRepository.checkStudentLessonsAndIntersections(any(), any(), any()))
                .willReturn(validation);
        given(bookingRepository.save(any())).willReturn(
                Booking.builder()
                        .id(bookingId)
                        .startTime(baseTime)
                        .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                        .status(BookingStatus.SCHEDULED)
                        .build()
        );

        var response = bookingService.createBooking(validDto);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        assertEquals(baseTime, response.startTime());
        verify(bookingRepository, times(1))
                .checkStudentLessonsAndIntersections(any(), any(), any());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException, если активных уроков уже 2")
    void shouldThrowSlotConflictExceptionWhenMaxLessonsReached() {
        var validation = new BookingValidationResult(2, 0);
        given(bookingRepository.checkStudentLessonsAndIntersections(any(), any(), any()))
                .willReturn(validation);

        var exception = assertThrows(SlotConflictException.class, () ->
                bookingService.createBooking(validDto)
        );

        assertEquals("Вы не можете записаться более чем на 2 урока одновременно.",
                exception.getMessage());
        verify(bookingRepository, times(1))
                .checkStudentLessonsAndIntersections(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException, если время пересекается с существующим")
    void shouldThrowSlotConflictExceptionWhenTimeOverlaps() {
        var validation = new BookingValidationResult(0, 1);
        given(bookingRepository.checkStudentLessonsAndIntersections(any(), any(), any()))
                .willReturn(validation);
        var exception = assertThrows(SlotConflictException.class, () ->
                bookingService.createBooking(validDto)
        );

        assertEquals("Данное время уже забронировано. Пожалуйста, выберите другое время.",
                exception.getMessage());
        verify(bookingRepository, times(1))
                .checkStudentLessonsAndIntersections(any(), any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен успешно получать доступные слоты за день за вычетом занятых")
    void shouldReturnAvailableSlotsExcludingBusyTimes() {
        var dayStart = baseTime.truncatedTo(ChronoUnit.DAYS);
        var busyBooking = Booking.builder()
                .startTime(dayStart.plus(10, ChronoUnit.HOURS))
                .build();
        given(bookingRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(any(), any(), any()))
                .willReturn(List.of(busyBooking));

        var result = bookingService.getAvailableSlots(dayStart);

        assertNotNull(result);
        assertFalse(result.contains(busyBooking.getStartTime()));
        verify(bookingRepository, times(1))
                .findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(any(), any(), any());
    }

    @Test
    @DisplayName("Должен успешно получать список активных записей студента")
    void shouldReturnActiveBookingsForStudent() {
        var activeBooking = Booking.builder()
                .id(bookingId)
                .startTime(baseTime)
                .endTime(baseTime.plus(60, ChronoUnit.MINUTES))
                .status(BookingStatus.SCHEDULED)
                .build();
        given(bookingRepository.findByStatusInAndStudentId(any(), any()))
                .willReturn(List.of(activeBooking));

        var result = bookingService.getActiveBookings(studentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingId, result.getFirst().bookingId());
        verify(bookingRepository, times(1)).findByStatusInAndStudentId(any(), any());
    }

    @Test
    @DisplayName("Должен успешно отменить запись (перевод статуса в CANCELED)")
    void shouldCancelBookingSuccessfully() {
        var activeBooking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.SCHEDULED)
                .build();
        var canceledBooking = activeBooking.toBuilder()
                .status(BookingStatus.CANCELED)
                .build();
        given(bookingRepository.findByStatusAndId(any(), any())).willReturn(Optional.of(activeBooking));
        given(bookingRepository.save(any())).willReturn(canceledBooking);

        var response = bookingService.deleteBooking(bookingId);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        verify(bookingRepository, times(1))
                .findByStatusAndId(any(), any());
        verify(bookingRepository, times(1))
                .save(argThat(b -> b.getStatus() == BookingStatus.CANCELED));
    }

    @Test
    @DisplayName("Должен выбросить SlotConflictException при отмене, если запись не найдена")
    void shouldThrowSlotConflictExceptionWhenBookingNotFoundForCancellation() {
        given(bookingRepository.findByStatusAndId(any(), any()))
                .willReturn(Optional.empty());

        var exception = assertThrows(SlotConflictException.class, () ->
                bookingService.deleteBooking(bookingId)
        );

        assertEquals("Не удалось отменить запись. Похоже, она уже была удалена ранее.",
                exception.getMessage());
        verify(bookingRepository, times(1)).findByStatusAndId(any(), any());
        verify(bookingRepository, never()).save(any());
    }
}