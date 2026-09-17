package ru.videoplatform.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.booking.dto.BookingRequestDto;
import ru.videoplatform.booking.dto.BookingResponseDto;
import ru.videoplatform.booking.dto.NotificationRequestDto;
import ru.videoplatform.booking.service.AsyncNotificationService;
import ru.videoplatform.booking.service.BookingService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AsyncNotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BookingRequestDto requestDto) {
        var booking = bookingService.createBooking(requestDto);
        notificationService.createNotificationAsync(
                systemToken,
                NotificationRequestDto.from(requestDto, booking.bookingId())
        );
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<Instant>> getAvailableSlots(
            @RequestParam("bookingDay") Instant bookingDay) {
        return ResponseEntity.ok(bookingService.getAvailableSlots(bookingDay));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BookingResponseDto>> getActiveBooking(
            @RequestParam("studentId") UUID studentId) {
        return ResponseEntity.ok(bookingService.getActiveBookings(studentId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BookingResponseDto> deleteBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingId") UUID bookingId) {
        var deleteBooking = bookingService.deleteBooking(bookingId);
                notificationService.deleteNotificationAsync(systemToken, bookingId);
        return ResponseEntity.ok(deleteBooking);
    }
}
