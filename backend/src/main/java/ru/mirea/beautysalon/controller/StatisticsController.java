package ru.mirea.beautysalon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.statistics.StatisticsResponse;
import ru.mirea.beautysalon.service.StatisticsService;

@Tag(name = "Статистика", description = "Дашборд ключевых показателей работы салона")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "Получить сводную статистику салона")
    @GetMapping
    public StatisticsResponse getStatistics() {
        return statisticsService.getStatistics();
    }
}