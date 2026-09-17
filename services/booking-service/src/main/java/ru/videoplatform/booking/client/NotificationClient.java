package ru.videoplatform.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.videoplatform.booking.dto.NotificationRequestDto;

import java.util.UUID;

@FeignClient(
        name = "notification-service",
        url = "${services.notification-service.uri}/api/v1/notifications"
)
public interface NotificationClient {

    @PostMapping("/create")
    void createNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody NotificationRequestDto requestDto);

    @PostMapping("/delete")
    void deleteNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody UUID bookingId);
}
