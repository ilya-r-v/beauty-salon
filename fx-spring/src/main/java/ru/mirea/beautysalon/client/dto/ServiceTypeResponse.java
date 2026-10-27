package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

public record ServiceTypeResponse(
        UUID id,
        String name
) {}
