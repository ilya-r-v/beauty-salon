package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String phone,
        String role,
        Boolean isMaster
) {}
