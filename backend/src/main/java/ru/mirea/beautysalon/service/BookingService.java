package ru.mirea.beautysalon.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.domain.Booking;
import ru.mirea.beautysalon.domain.BookingStatus;
import ru.mirea.beautysalon.domain.Client;
import ru.mirea.beautysalon.domain.ServiceVariant;
import ru.mirea.beautysalon.domain.TimeSlot;
import ru.mirea.beautysalon.dto.PageResult;
import ru.mirea.beautysalon.dto.booking.BookingEdit;
import ru.mirea.beautysalon.dto.booking.BookingResponse;
import ru.mirea.beautysalon.dto.booking.CreateBookingRequest;
import ru.mirea.beautysalon.dto.booking.StatusRequest;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.filter.BookingFilter;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;
import ru.mirea.beautysalon.repository.TimeSlotRepository;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ServiceVariantRepository serviceVariantRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        return create(request, BookingStatus.PENDING, false, null);
    }

    // CSV can carry historical bookings, including cancelled visits sharing a slot.
    @Transactional
    public BookingResponse create(
        CreateBookingRequest request,
        BookingStatus status,
        boolean archived,
        java.math.BigDecimal importedPrice
    ) {
        Client client = clientRepository
            .findById(request.getClientId())
            .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        TimeSlot slot = timeSlotRepository
            .findById(request.getSlotId())
            .orElseThrow(() -> new EntityNotFoundException("Слот не найден"));

        ServiceVariant variant = serviceVariantRepository
            .findById(request.getServiceVariantId())
            .orElseThrow(() -> new EntityNotFoundException("Вариант услуги не найден"));

        if (
            status != BookingStatus.CANCELLED &&
            bookingRepository.existsByTimeSlotIdAndStatusNot(slot.getId(), BookingStatus.CANCELLED)
        ) {
            throw new BusinessException("Выбранный слот уже занят");
        }

        if (!variant.getService().getMaster().getId().equals(slot.getMaster().getId())) {
            throw new BusinessException("Выбранная услуга не оказывается мастером данного слота");
        }

        if (!variant.getService().getServiceType().isActive()) throw new BusinessException(
            "Тип услуги отключён"
        );
        Booking booking = Booking.builder()
            .externalCode(
                request.getExternalCode() == null || request.getExternalCode().isBlank()
                    ? UUID.randomUUID().toString()
                    : request.getExternalCode().strip()
            )
            .comment(request.getComment())
            .prepaid(request.isPrepaid())
            .tags(
                request.getTags() == null
                    ? new java.util.LinkedHashSet<>()
                    : new java.util.LinkedHashSet<>(request.getTags())
            )
            .client(client)
            .timeSlot(slot)
            .serviceVariant(variant)
            .bookedPrice(importedPrice == null ? variant.getPrice() : importedPrice)
            .archived(archived)
            .status(status)
            .build();

        return mapToResponse(bookingRepository.saveAndFlush(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        Booking booking = findVisibleBooking(id);
        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public PageResult<BookingResponse> list(BookingFilter filter, int page, int size) {
        var query = filter.specification().and(visibleToCurrentUser());
        return PageResult.of(
            bookingRepository
                .findAll(query, PageResult.request(page, size, filter.sort()))
                .map(this::mapToResponse)
        );
    }

    @Transactional
    public BookingResponse edit(UUID id, BookingEdit request) {
        Booking booking = findVisibleBooking(id);
        if (!java.util.Objects.equals(booking.getVersion(), request.version())) throw new ConflictException(
            "Версия записи устарела"
        );
        booking.setComment(request.comment());
        booking.setPrepaid(request.prepaid());
        booking.getTags().clear();
        booking.getTags().addAll(request.tags());
        return mapToResponse(bookingRepository.saveAndFlush(booking));
    }

    @Transactional
    public BookingResponse updateStatus(UUID id, StatusRequest request) {
        Booking booking = findVisibleBooking(id);

        if (
            !java.util.Objects.equals(booking.getVersion(), request.getVersion())
        ) throw new ConflictException("Версия записи устарела");
        BookingStatus current = booking.getStatus();
        BookingStatus target = request.getStatus();

        if (target == null) {
            throw new BusinessException("Новый статус не может быть пустым");
        }

        validateStateTransition(current, target);

        booking.setStatus(target);
        return mapToResponse(bookingRepository.saveAndFlush(booking));
    }

    @Transactional
    public void deleteBooking(UUID id) {
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Запись с ID " + id + " не найдена"));

        booking.setArchived(true);
        bookingRepository.save(booking);
    }

    @Transactional
    public BookingResponse restore(UUID id) {
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));
        booking.setArchived(false);
        return mapToResponse(bookingRepository.saveAndFlush(booking));
    }

    @Transactional
    public void permanentDelete(UUID id) {
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));
        bookingRepository.delete(booking);
    }

    private void validateStateTransition(BookingStatus current, BookingStatus target) {
        boolean isValid = switch (current) {
            case PENDING -> target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED;
            case CONFIRMED -> target == BookingStatus.COMPLETED || target == BookingStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!isValid) {
            throw new BusinessException("Недопустимый переход статуса из " + current + " в " + target);
        }
    }

    private Specification<Booking> visibleToCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UUID userId)) {
            return (root, query, cb) -> cb.disjunction();
        }

        var roles = auth.getAuthorities();

        if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            return (root, query, cb) -> cb.conjunction();
        }
        if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_MASTER"))) {
            return (root, query, cb) -> cb.equal(root.get("timeSlot").get("master").get("id"), userId);
        }
        if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENT"))) {
            return (root, query, cb) -> cb.equal(root.get("client").get("id"), userId);
        }

        return (root, query, cb) -> cb.disjunction();
    }

    private Booking findVisibleBooking(UUID id) {
        Specification<Booking> byId = (root, query, cb) -> cb.equal(root.get("id"), id);

        return bookingRepository
            .findOne(byId.and(visibleToCurrentUser()))
            .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));
    }

    private BookingResponse mapToResponse(Booking booking) {
        TimeSlot slot = booking.getTimeSlot();
        ServiceVariant variant = booking.getServiceVariant();
        Client client = booking.getClient();

        return BookingResponse.builder()
            .id(booking.getId())
            .bookingNumber(booking.getBookingNumber())
            .externalCode(booking.getExternalCode())
            .comment(booking.getComment())
            .prepaid(booking.isPrepaid())
            .priority(booking.getPriority())
            .tags(java.util.Set.copyOf(booking.getTags()))
            .updatedAt(booking.getUpdatedAt())
            .archived(booking.isArchived())
            .version(booking.getVersion())
            .clientId(client.getId())
            .clientName(client.getName())
            .slot(
                BookingResponse.SlotDto.builder()
                    .id(slot.getId())
                    .masterId(slot.getMaster().getId())
                    .masterName(slot.getMaster().getClient().getName())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .build()
            )
            .serviceVariantId(variant.getId())
            .serviceVariantTitle(variant.getTitle())
            .price(booking.getBookedPrice())
            .status(booking.getStatus())
            .createdAt(booking.getCreatedAt())
            .build();
    }
}
