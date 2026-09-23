package ru.videoplatform.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.videoplatform.notification.dto.BotNotificationRequestDto;

@FeignClient(
        name = "bot-service",
        url = "${services.bot-service.uri}/api/v1/bot"
)
public interface BotClient {

    @PostMapping("/internal/notifications")
    void sendNotification(@RequestBody BotNotificationRequestDto dto);
}
