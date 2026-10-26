package ru.mirea.beautysalon.dto.master;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {
    private UUID id;
    private UUID masterId;
    private String masterName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
}