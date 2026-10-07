package ru.videoplatform.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.booking.dto.BookingCalendarResponseDto;

import java.util.UUID;

@FeignClient(
        name = "signaling-service",
        url = "${services.signaling-service.uri}/signaling"
)
public interface SignalingClient {

    @PostMapping("/create")
    void createSignaling(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody BookingCalendarResponseDto requestDto);

    @PostMapping("/delete")
    void deleteSignaling(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody UUID bookingId);
}
