package ru.videoplatform.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.notification.dto.NotificationRequestDto;
import ru.videoplatform.notification.service.NotificationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestBody NotificationRequestDto requestDto) {
        notificationService.createNotification(requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteNotification(
            @RequestHeader("Authorization") String systemToken,
            @RequestParam UUID bookingId) {
        notificationService.deleteNotification(bookingId);
        return ResponseEntity.ok().build();
    }
}
