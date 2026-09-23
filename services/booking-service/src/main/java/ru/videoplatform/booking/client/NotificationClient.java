package ru.videoplatform.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.booking.dto.NotificationRequestDto;

import java.util.UUID;

@FeignClient(
        name = "notification-service",
        url = "${services.notification-service.uri}/notifications"
)
public interface NotificationClient {

    @PostMapping("/create")
    void createNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody NotificationRequestDto requestDto);

    @DeleteMapping("/delete")
    void deleteNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam("bookingId") UUID bookingId);
}
