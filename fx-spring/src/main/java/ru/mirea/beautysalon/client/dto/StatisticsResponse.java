package ru.mirea.beautysalon.client.dto;

import java.math.BigDecimal;
import java.util.Map;

// Ответ /api/statistics
public record StatisticsResponse(
        long totalClients,
        long totalMasters,
        long totalBookings,
        Map<String, Long> bookingsByStatus,  // ключи: PENDING, CONFIRMED, COMPLETED, CANCELLED
        BigDecimal totalRevenue,
        BigDecimal averageCheck
) {}
