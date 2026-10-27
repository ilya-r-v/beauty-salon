package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

public record MasterResponse(
        UUID clientId,
        String name,
        String phone,
        String avatarUrl,
        String description,
        Double rating
) {}
