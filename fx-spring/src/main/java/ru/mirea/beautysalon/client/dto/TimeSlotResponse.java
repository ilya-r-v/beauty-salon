package ru.mirea.beautysalon.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// Ответ /api/time-slots (свободные слоты мастера)
public record TimeSlotResponse(
                UUID id,
                UUID masterId,
                String masterName,
                OffsetDateTime startTime,
                OffsetDateTime endTime) {
        @Override
        public String toString() {
                var zone = java.time.ZoneId.systemDefault();
                var date = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                var time = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                return startTime.atZoneSameInstant(zone).format(date)
                                + " - " + endTime.atZoneSameInstant(zone).format(time);
        }
}
