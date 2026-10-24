package ru.mirea.beautysalon.dto.statistics;

import java.math.BigDecimal;
import java.util.Map;

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
public class StatisticsResponse {
    private long totalClients;
    private long totalMasters;
    private long totalBookings;
    private Map<BookingStatus, Long> bookingsByStatus;
    private BigDecimal totalRevenue;
    private BigDecimal averageCheck;
}