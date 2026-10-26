package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

// Ответ на /api/auth/register и /api/auth/login
public record AuthResponse(
        String token,
        UUID id,
        String name,
        String phone,
        String role
) {}
