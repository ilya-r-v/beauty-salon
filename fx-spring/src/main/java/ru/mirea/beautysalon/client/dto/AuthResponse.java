package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID id,
        String name,
        String phone,
        String role
) {}
