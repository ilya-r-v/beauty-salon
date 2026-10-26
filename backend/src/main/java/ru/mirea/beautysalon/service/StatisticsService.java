package ru.mirea.beautysalon.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.statistics.StatisticsResponse;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.Role;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterRepository;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ClientRepository clientRepository;
    private final MasterRepository masterRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {

        long totalClients = clientRepository.countByRole(Role.CLIENT);
        long totalMasters = masterRepository.count();
        long totalBookings = bookingRepository.count();

        Map<BookingStatus, Long> bookingsByStatus = new EnumMap<>(BookingStatus.class);
        for (BookingStatus status : BookingStatus.values()) {
            bookingsByStatus.put(status, bookingRepository.countByStatus(status));
        }

        BigDecimal totalRevenue = bookingRepository.sumPriceByStatus(BookingStatus.COMPLETED);
        long completedCount = bookingsByStatus.getOrDefault(BookingStatus.COMPLETED, 0L);

        BigDecimal averageCheck = (completedCount > 0)
                ? totalRevenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return StatisticsResponse.builder()
                .totalClients(totalClients)
                .totalMasters(totalMasters)
                .totalBookings(totalBookings)
                .bookingsByStatus(bookingsByStatus)
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .averageCheck(averageCheck)
                .build();
    }
}