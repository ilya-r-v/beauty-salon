package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Контракт доступа к данным для основной сущности Booking.

public interface BookingRepository {

    Booking save(Booking booking);

    List<Booking> findAll();

    Optional<Booking> findById(UUID id);

    Booking update(Booking booking);

    void deleteById(UUID id);

    List<Booking> searchByClientName(String namePart);
    List<Booking> searchByServiceTitle(String titlePart);

    List<Booking> filterByStatus(BookingStatus status);
    List<Booking> filterByMaster(UUID masterId);
    List<Booking> filterByDateRange(LocalDateTime from, LocalDateTime to);

    boolean existsOverlapForMaster(UUID masterId, LocalDateTime time);
}
