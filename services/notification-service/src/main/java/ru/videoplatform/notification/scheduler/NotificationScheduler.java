package ru.videoplatform.notification.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.videoplatform.notification.client.BotClient;
import ru.videoplatform.notification.dto.BotNotificationRequestDto;
import ru.videoplatform.notification.service.NotificationService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final BotClient botClient;

    private final ZoneId moscowZone = ZoneId.of("Europe/Moscow");

    private final DateTimeFormatter dayFormatter = DateTimeFormatter
            .ofPattern("d MMMM", Locale.of("ru"))
            .withZone(moscowZone);

    private final DateTimeFormatter timeFormatter = DateTimeFormatter
            .ofPattern("HH:mm")
            .withZone(ZoneOffset.UTC);

    @Scheduled(cron = "0 0 9-19 * * *", zone = "Europe/Moscow")
    public void sendNotifications() {
        var timeDay = Instant.now().atZone(moscowZone);

        notificationService.getAndDeleteNotifications(timeDay.plusHours(1).toInstant())
                .ifPresent(notification -> {
                    var greeting = switch (timeDay.getHour()) {
                        case 9, 10, 11 -> "Доброе утро ";
                        case 12, 13, 14, 15, 16 -> "Добрый день ";
                        default -> "Добрый вечер ";
                    };

                    var message = new StringBuilder()
                            .append("👋 ")
                            .append(greeting);

                    var dateParts = dayFormatter.format(notification.getStartTime()).split(" ");
                    message.append(String.format(
                            "%s! Не забудьте, что сегодня %s %s%s у вас занятие с %s до %s.",
                            notification.getFirstName(),
                            dateParts[0],
                            dateParts[1].substring(0, 1).toUpperCase(),
                            dateParts[1].substring(1),
                            timeFormatter.format(notification.getStartTime()),
                            timeFormatter.format(notification.getEndTime())
                    ));

                    var requestDto = new BotNotificationRequestDto(
                            notification.getChatId(),
                            message.toString()
                    );

                    botClient.sendNotification(requestDto);
                });
    }
}
