package ru.mirea.beautysalon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.booking.BookingResponse;
import ru.mirea.beautysalon.dto.booking.CreateBookingRequest;
import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.Client;
import ru.mirea.beautysalon.entity.ServiceVariant;
import ru.mirea.beautysalon.entity.TimeSlot;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
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
                Client client = clientRepository.findById(request.getClientId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Клиент с ID " + request.getClientId() + " не найден"));

                TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Слот с ID " + request.getSlotId() + " не найден"));

                ServiceVariant variant = serviceVariantRepository.findById(request.getServiceVariantId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Вариант услуги с ID " + request.getServiceVariantId() + " не найден"));

                boolean isSlotTaken = bookingRepository.existsByTimeSlotIdAndStatusNot(
                                slot.getId(),
                                BookingStatus.CANCELLED);
                if (isSlotTaken) {
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

                Booking savedBooking = bookingRepository.save(booking);

                return mapToResponse(savedBooking);
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