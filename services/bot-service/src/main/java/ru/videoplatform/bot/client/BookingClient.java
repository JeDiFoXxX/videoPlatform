package ru.videoplatform.bot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.bot.dto.BotBookingRequestDto;
import ru.videoplatform.bot.dto.BookingBotResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "booking-service",
        url = "${services.booking-service.uri}/bookings"
)
public interface BookingClient {

    @PostMapping("/create")
    BookingBotResponseDto createBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BotBookingRequestDto requestDto
    );

    @GetMapping("/slots")
    List<Instant> getAvailableSlots(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingDay") Instant bookingDay
    );

    @GetMapping("/active")
    List<BookingBotResponseDto> getActiveBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("studentId") UUID studentId
    );

    @DeleteMapping("/delete")
    BookingBotResponseDto deleteBooking(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingId") UUID bookingId
    );
}
