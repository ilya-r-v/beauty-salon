package ru.mirea.beautysalon.client.dto;

import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String title,
        UUID masterId,
        String masterName,
        UUID serviceTypeId,
        String serviceTypeName
) {}
