package ru.videoplatform.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.videoplatform.booking.dto.BookingCalendarRequestDto;
import ru.videoplatform.booking.dto.BookingCalendarResponseDto;
import ru.videoplatform.booking.dto.BotBookingRequestDto;
import ru.videoplatform.booking.dto.BookingBotResponseDto;
import ru.videoplatform.booking.exception.SlotConflictException;
import ru.videoplatform.booking.model.Booking;
import ru.videoplatform.booking.model.BookingStatus;
import ru.videoplatform.booking.repository.BookingRepository;
import ru.videoplatform.booking.storage.BookingScheduleStorage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional
    public BookingBotResponseDto createBooking(BotBookingRequestDto dto) {
        var validation = bookingRepository.checkStudentBookingsAndIntersections(
                dto.studentId(), dto.startTime(), dto.endTime()
        );

        if (validation.activeBookingsCount() == 2) {
            throw new SlotConflictException(
                    "Вы не можете записаться более чем на 2 урока одновременно.");
        }

        if (validation.timeOverlapsCount() > 0) {
            throw new SlotConflictException(
                    "Данное время уже забронировано. Пожалуйста, выберите другое время.");
        }

        return BookingBotResponseDto.from(bookingRepository.save(dto.toEntity()));
    }

    @Transactional
    public List<Instant> getAvailableSlots(Instant bookingDay) {
        var busyBookings = bookingRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
                List.of(BookingStatus.SCHEDULED),
                bookingDay,
                bookingDay.plus(1, ChronoUnit.DAYS)
        );
        var busyTimes = busyBookings.stream()
                .map(Booking::getStartTime)
                .toList();
        var availableSlots = BookingScheduleStorage.getSlotsTemplate(bookingDay);
        availableSlots.removeAll(busyTimes);
        return availableSlots;
    }

    @Transactional
    public List<BookingBotResponseDto> getActiveBookings(UUID studentId) {
        var activeBookings = bookingRepository.findByStatusInAndStudentId(
                List.of(BookingStatus.SCHEDULED),
                studentId
        );

        return activeBookings.stream()
                .map(BookingBotResponseDto::from)
                .toList();
    }

    @Transactional
    public List<BookingCalendarResponseDto> getCalendarBookings(BookingCalendarRequestDto dto) {
        return bookingRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
                        dto.statuses(), dto.from(), dto.to())
                .stream()
                .map(BookingCalendarResponseDto::from)
                .toList();
    }

    @Transactional
    public BookingBotResponseDto deleteBooking(UUID id) {
        var activeBooking = bookingRepository.findByStatusAndId(BookingStatus.SCHEDULED, id)
                .orElseThrow(() -> new SlotConflictException(
                        "Не удалось отменить запись. Похоже, она уже была удалена ранее."));
        var deleteBooking = activeBooking.toBuilder()
                .status(BookingStatus.CANCELED)
                .build();
        return BookingBotResponseDto.from(bookingRepository.save(deleteBooking));
    }
}
