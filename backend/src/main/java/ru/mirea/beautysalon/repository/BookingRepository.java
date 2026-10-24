package ru.mirea.beautysalon.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    boolean existsByTimeSlotIdAndStatusNot(UUID timeSlotId, BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.serviceVariant.price), 0) FROM Booking b WHERE b.status = :status")
    BigDecimal sumPriceByStatus(@Param("status") BookingStatus status);
}