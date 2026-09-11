package ru.videoplatform.bot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.bot.dto.BookingRequestDto;
import ru.videoplatform.bot.dto.BookingResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "booking-service",
        url = "${services.booking-service.uri}/api/v1/bookings"
)
public interface BookingClient {

    @PostMapping("/create")
    boolean createBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BookingRequestDto requestDto
    );

    @GetMapping("/slots")
    List<Instant> getAvailableSlots(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingDay") Instant bookingDay
    );

    @GetMapping("/active")
    List<BookingResponseDto> getActiveBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("studentId") UUID studentId
    );

    @DeleteMapping("/delete")
    boolean deleteBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("lessonId") UUID lessonId
    );
}
