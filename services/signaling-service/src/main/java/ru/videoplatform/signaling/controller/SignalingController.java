package ru.videoplatform.signaling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.videoplatform.signaling.dto.BookingCalendarResponseDto;
import ru.videoplatform.signaling.service.AsyncService;

import java.util.UUID;

@RestController
@RequestMapping("/signaling")
@RequiredArgsConstructor
public class SignalingController {

    private final AsyncService asyncService;

    @PostMapping("/create")
    public ResponseEntity<Void> createSignaling(
            @RequestBody BookingCalendarResponseDto requestDto) {
        asyncService.sendCreateEvent(requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteSignaling(
            @RequestBody UUID bookingId) {
        asyncService.sendDeleteEvent(bookingId);
        return ResponseEntity.ok().build();
    }
}
