package ru.mirea.beautysalon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.master.MasterResponse;
import ru.mirea.beautysalon.service.MasterService;
import ru.mirea.beautysalon.dto.master.TimeSlotResponse;

@Tag(name = "Мастера", description = "Просмотр профилей мастеров салона")
@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterService masterService;

    @Operation(summary = "Получить список всех мастеров")
    @GetMapping
    public List<MasterResponse> getAllMasters() {
        return masterService.getAllMasters();
    }

    @Operation(summary = "Получить профиль мастера по ID")
    @GetMapping("/{clientId}")
    public MasterResponse getMasterById(@PathVariable UUID clientId) {
        return masterService.getMasterById(clientId);
    }
}