package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    boolean existsByTimeSlotIdAndStatusNot(UUID timeSlotId, BookingStatus status);
}