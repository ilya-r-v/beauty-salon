package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

// Ответ /api/masters и /api/masters/{clientId}
public record MasterResponse(
        UUID clientId,
        String name,
        String phone,
        String avatarUrl,
        String description,
        Double rating
) {}
