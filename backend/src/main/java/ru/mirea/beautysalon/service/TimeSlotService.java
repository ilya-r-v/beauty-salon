package ru.mirea.beautysalon.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.master.TimeSlotResponse;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.TimeSlot;
import ru.mirea.beautysalon.repository.TimeSlotRepository;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getFreeSlots(UUID masterId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<TimeSlot> slots = (masterId == null)
                ? timeSlotRepository.findAllFreeSlots(now, BookingStatus.CANCELLED)
                : timeSlotRepository.findFreeSlots(masterId, now, BookingStatus.CANCELLED);
        return slots.stream().map(this::toResponse).toList();
    }

    private TimeSlotResponse toResponse(TimeSlot s) {
        return TimeSlotResponse.builder()
                .id(s.getId())
                .masterId(s.getMaster().getId())
                .masterName(s.getMaster().getClient().getName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();
    }
}