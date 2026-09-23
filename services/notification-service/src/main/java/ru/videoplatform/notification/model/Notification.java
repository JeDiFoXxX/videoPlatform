package ru.videoplatform.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.videoplatform.notification.dto.NotificationRequestDto;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    public static Notification from(NotificationRequestDto dto) {
        return new Notification(
                null,
                dto.chatId(),
                dto.bookingId(),
                dto.firstName(),
                dto.startTime(),
                dto.endTime()
        );
    }

}
