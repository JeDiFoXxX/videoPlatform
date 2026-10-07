package ru.videoplatform.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.booking.dto.*;
import ru.videoplatform.booking.service.AsyncService;
import ru.videoplatform.booking.service.BookingService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AsyncService asyncService;

    @PostMapping("/create")
    public ResponseEntity<BookingBotResponseDto> createBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BotBookingRequestDto requestDto) {
        var booking = bookingService.createBooking(requestDto);
        asyncService.createNotificationAsync(
                systemToken,
                BookingNotificationRequestDto.from(requestDto, booking.bookingId())
        );
        asyncService.createSignalingAsync(
                systemToken,
                BookingCalendarResponseDto.from(booking));
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
        asyncService.deleteNotificationAsync(systemToken, bookingId);
        asyncService.deleteSignalingAsync(systemToken, bookingId);
        return ResponseEntity.ok(deleteBooking);
    }

    @PatchMapping("/finish")
    public ResponseEntity<Void> finishBooking(
            @RequestParam("bookingId") UUID bookingId) {
        bookingService.finishBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/cancel")
    public ResponseEntity<Void> cancelBooking(
            @RequestParam("bookingId") UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}
