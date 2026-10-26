package ru.mirea.beautysalon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.master.TimeSlotResponse;
import ru.mirea.beautysalon.service.TimeSlotService;

@Tag(name = "Слоты", description = "Свободные временные слоты мастеров")
@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @Operation(summary = "Свободные слоты (все или конкретного мастера)")
    @GetMapping
    public List<TimeSlotResponse> getFreeSlots(@RequestParam(required = false) UUID masterId) {
        return timeSlotService.getFreeSlots(masterId);
    }
}