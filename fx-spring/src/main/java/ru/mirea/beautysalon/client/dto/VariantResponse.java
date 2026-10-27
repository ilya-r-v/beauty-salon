package ru.mirea.beautysalon.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        UUID serviceId,
        String serviceTitle
) {}
