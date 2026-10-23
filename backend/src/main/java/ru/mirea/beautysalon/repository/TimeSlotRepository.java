package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {
}
