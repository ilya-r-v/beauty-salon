package ru.mirea.beautysalon.dto.service;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private UUID serviceId;
    private String serviceTitle;
}