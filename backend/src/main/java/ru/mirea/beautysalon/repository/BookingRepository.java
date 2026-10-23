package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByTimeSlotIdAndStatusNot(UUID timeSlotId, BookingStatus status);
}