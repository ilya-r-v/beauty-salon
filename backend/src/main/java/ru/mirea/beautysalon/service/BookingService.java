package ru.mirea.beautysalon.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.booking.BookingResponse;
import ru.mirea.beautysalon.dto.booking.CreateBookingRequest;
import ru.mirea.beautysalon.dto.booking.StatusRequest;
import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.Client;
import ru.mirea.beautysalon.entity.ServiceVariant;
import ru.mirea.beautysalon.entity.TimeSlot;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;
import ru.mirea.beautysalon.repository.TimeSlotRepository;
import ru.mirea.beautysalon.repository.specification.BookingSpecifications;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ServiceVariantRepository serviceVariantRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Слот не найден"));

        ServiceVariant variant = serviceVariantRepository.findById(request.getServiceVariantId())
                .orElseThrow(() -> new EntityNotFoundException("Вариант услуги не найден"));

        if (bookingRepository.existsByTimeSlotIdAndStatusNot(slot.getId(), BookingStatus.CANCELLED)) {
            throw new BusinessException("Выбранный слот уже занят");
        }

        if (!variant.getService().getMaster().getId().equals(slot.getMaster().getId())) {
            throw new BusinessException("Выбранная услуга не оказывается мастером данного слота");
        }

        Booking booking = Booking.builder()
                .client(client)
                .timeSlot(slot)
                .serviceVariant(variant)
                .status(BookingStatus.PENDING)
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись с ID " + id + " не найдена"));
        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(
            String search,
            String searchBy,
            BookingStatus status,
            UUID masterId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String sortBy) {
        Specification<Booking> spec = BookingSpecifications.filterBookings(
                search, searchBy, status, masterId, dateFrom, dateTo);

        Sort sort = "status".equalsIgnoreCase(sortBy)
                ? Sort.by(Sort.Direction.ASC, "status")
                : Sort.by(Sort.Direction.ASC, "timeSlot.startTime");

        return bookingRepository.findAll(spec, sort)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BookingResponse updateStatus(UUID id, StatusRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись с ID " + id + " не найдена"));

        BookingStatus current = booking.getStatus();
        BookingStatus target = request.getStatus();

        if (target == null) {
            throw new BusinessException("Новый статус не может быть пустым");
        }

        validateStateTransition(current, target);

        booking.setStatus(target);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public void deleteBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись с ID " + id + " не найдена"));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("Нельзя удалить завершенную запись");
        }

        bookingRepository.delete(booking);
    }

    private void validateStateTransition(BookingStatus current, BookingStatus target) {
        boolean isValid = switch (current) {
            case PENDING -> (target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED);
            case CONFIRMED -> (target == BookingStatus.COMPLETED || target == BookingStatus.CANCELLED);
            case COMPLETED, CANCELLED -> false;
        };

        if (!isValid) {
            throw new BusinessException("Недопустимый переход статуса из " + current + " в " + target);
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        TimeSlot slot = booking.getTimeSlot();
        ServiceVariant variant = booking.getServiceVariant();
        Client client = booking.getClient();

        return BookingResponse.builder()
                .id(booking.getId())
                .clientId(client.getId())
                .clientName(client.getName())
                .slot(BookingResponse.SlotDto.builder()
                        .id(slot.getId())
                        .masterId(slot.getMaster().getId())
                        .masterName(slot.getMaster().getClient().getName())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .build())
                .serviceVariantId(variant.getId())
                .serviceVariantTitle(variant.getTitle())
                .price(variant.getPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}