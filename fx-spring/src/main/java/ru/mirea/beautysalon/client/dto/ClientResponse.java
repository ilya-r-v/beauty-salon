package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

// Ответ /api/clients и /api/clients/{id}
public record ClientResponse(
        UUID id,
        String name,
        String phone,
        String role,
        Boolean isMaster
) {}
