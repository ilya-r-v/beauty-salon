package ru.mirea.beautysalon.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Query("""
            SELECT s FROM TimeSlot s
            JOIN FETCH s.master m JOIN FETCH m.client
            WHERE s.startTime > :now
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.timeSlot = s AND b.status <> :cancelled)
            ORDER BY s.startTime
            """)
    List<TimeSlot> findAllFreeSlots(@Param("now") OffsetDateTime now,
            @Param("cancelled") BookingStatus cancelled);

    @Query("""
            SELECT s FROM TimeSlot s
            JOIN FETCH s.master m JOIN FETCH m.client
            WHERE m.id = :masterId
              AND s.startTime > :now
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.timeSlot = s AND b.status <> :cancelled)
            ORDER BY s.startTime
            """)
    List<TimeSlot> findFreeSlots(@Param("masterId") UUID masterId,
            @Param("now") OffsetDateTime now,
            @Param("cancelled") BookingStatus cancelled);
}