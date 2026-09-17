package ru.videoplatform.booking.storage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookingScheduleStorage {

    private static final List<Integer> HOURS_TEMPLATE =
            List.of(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

    private BookingScheduleStorage() { }

    public static List<Instant> getSlotsTemplate(Instant bookingDay) {
        return new ArrayList<>(
                HOURS_TEMPLATE.stream()
                        .map(hour -> bookingDay.plus(hour, ChronoUnit.HOURS))
                        .toList()
        );
    }
}
