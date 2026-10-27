package ru.mirea.beautysalon.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID clientId,
        String clientName,
        SlotDto slot,
        UUID serviceVariantId,
        String serviceVariantTitle,
        java.math.BigDecimal price,
        String status,
        OffsetDateTime createdAt
) {
    public record SlotDto(
            UUID id,
            UUID masterId,
            String masterName,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {}
}
