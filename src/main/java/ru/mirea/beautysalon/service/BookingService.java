package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


// Бизнес-правила по записи на услугу. Нельзя создать запись с несуществующим клиентом. Нельзя создать запись на время в прошлом.
// Нельзя создать запись, если мастер уже занят в это время. Нельзя выполнить запрещённый переход статуса.
// Нельзя изменять/удалять запись в статусе COMPLETED. Нельзя создать запись без указания варианта услуги (не должно быть null).

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, Set.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED, Set.of()
    );

    public BookingService(BookingRepository bookingRepository, ClientRepository clientRepository) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
    }

    public Booking create(Booking booking) {
        validateForCreate(booking);
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        return bookingRepository.save(booking);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking getById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись с id=" + id + " не найдена"));
    }

    public Booking update(Booking booking) {
        Booking existing = getById(booking.getId());
        if (existing.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Нельзя изменять завершённую запись");
        }
        if (booking.getStatus() != existing.getStatus()) {
            validateTransition(existing.getStatus(), booking.getStatus());
        }
        if (booking.getServiceVariantId() == null) {
            throw new BusinessException("Не указан вариант услуги");
        }
        return bookingRepository.update(booking);
    }

    public void changeStatus(UUID id, BookingStatus newStatus) {
        Booking existing = getById(id);
        validateTransition(existing.getStatus(), newStatus);
        existing.setStatus(newStatus);
        bookingRepository.update(existing);
    }

    public void delete(UUID id) {
        Booking existing = getById(id);
        if (existing.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Нельзя удалить завершённую запись");
        }
        bookingRepository.deleteById(id);
    }

    // поиск
    public List<Booking> searchByClientName(String namePart) {
        return bookingRepository.searchByClientName(namePart);
    }

    public List<Booking> searchByServiceTitle(String titlePart) {
        return bookingRepository.searchByServiceTitle(titlePart);
    }

    // фильтрация
    public List<Booking> filterByStatus(BookingStatus status) {
        return bookingRepository.filterByStatus(status);
    }

    public List<Booking> filterByMaster(UUID masterId) {
        return bookingRepository.filterByMaster(masterId);
    }

    public List<Booking> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        return bookingRepository.filterByDateRange(from, to);
    }

    // сортировка
    public List<Booking> sortByTime(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getTime))
                .collect(Collectors.toList());
    }

    public List<Booking> sortByStatus(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getStatus))
                .collect(Collectors.toList());
    }

    // статистика
    public Map<BookingStatus, Long> countByStatus() {
        return bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
    }

    //проверки бизнес правил

    private void validateForCreate(Booking booking) {
        if (booking.getClientId() == null || !clientRepository.existsById(booking.getClientId())) {
            throw new BusinessException("Указан несуществующий клиент");
        }
        if (booking.getServiceVariantId() == null) {
            throw new BusinessException("Не указан вариант услуги");
        }
        if (booking.getMasterId() == null) {
            throw new BusinessException("Не указан мастер");
        }
        if (booking.getTime() == null || booking.getTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Нельзя создать запись на прошедшее время");
        }
        if (bookingRepository.existsOverlapForMaster(booking.getMasterId(), booking.getTime())) {
            throw new BusinessException("Мастер уже занят в это время");
        }
    }

    private void validateTransition(BookingStatus from, BookingStatus to) {
        if (from == to) return;
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BusinessException("Недопустимый переход статуса: " + from + " -> " + to);
        }
    }
}
