package ru.mirea.beautysalon.dto.booking;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.beautysalon.entity.BookingStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private SlotDto slot;
    private UUID serviceVariantId;
    private String serviceVariantTitle;
    private BigDecimal price;
    private BookingStatus status;
    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotDto {
        private UUID id;
        private UUID masterId;
        private String masterName;
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
    }
}