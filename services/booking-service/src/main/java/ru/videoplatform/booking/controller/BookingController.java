package ru.videoplatform.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.booking.dto.*;
import ru.videoplatform.booking.model.BookingStatus;
import ru.videoplatform.booking.service.AsyncNotificationService;
import ru.videoplatform.booking.service.BookingService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AsyncNotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity<BookingBotResponseDto> createBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BotBookingRequestDto requestDto) {
        var booking = bookingService.createBooking(requestDto);
        notificationService.createNotificationAsync(
                systemToken,
                BookingNotificationRequestDto.from(requestDto, booking.bookingId())
        );
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<Instant>> getAvailableSlots(
            @RequestParam("bookingDay") Instant bookingDay) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(bookingDay));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BookingBotResponseDto>> getActiveBooking(
            @RequestParam("studentId") UUID studentId) {
        return ResponseEntity.ok(bookingService.getActiveBookings(studentId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<BookingCalendarResponseDto>> getCalendarBookings(
            BookingCalendarRequestDto requestDto) {
        return ResponseEntity.ok(bookingService.getCalendarBookings(requestDto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BookingBotResponseDto> deleteBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingId") UUID bookingId) {
        var deleteBooking = bookingService.deleteBooking(bookingId);
        notificationService.deleteNotificationAsync(systemToken, bookingId);
        return ResponseEntity.ok(deleteBooking);
    }
}
