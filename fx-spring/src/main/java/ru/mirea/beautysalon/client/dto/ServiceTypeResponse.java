package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

// Ответ /api/service-types
public record ServiceTypeResponse(
        UUID id,
        String name
) {}
