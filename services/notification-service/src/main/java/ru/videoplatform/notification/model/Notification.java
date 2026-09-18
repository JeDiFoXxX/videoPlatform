package ru.videoplatform.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.videoplatform.notification.dto.NotificationRequestDto;

import java.time.Instant;

@Document(collection = "notifications")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Notification {

    @Id
    private String id;

    @Indexed
    private Instant startTime;

    private Long chatId;
    private String bookingId;
    private String firstName;
    private Instant endTime;

    public static Notification from(NotificationRequestDto dto) {
        return new Notification(
                null,
                dto.startTime(),
                dto.chatId(),
                dto.bookingId().toString(),
                dto.firstName(),
                dto.endTime()
        );
    }
}
