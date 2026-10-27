package ru.mirea.beautysalon.client.dto;

import java.math.BigDecimal;
import java.util.Map;

public record StatisticsResponse(
        long totalClients,
        long totalMasters,
        long totalBookings,
        Map<String, Long> bookingsByStatus,
        BigDecimal totalRevenue,
        BigDecimal averageCheck
) {}
