package ru.mirea.beautysalon.dto.service;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequest {

    @NotBlank(message = "Название варианта услуги не может быть пустым")
    private String title;

    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше нуля")
    private BigDecimal price;

    @NotNull(message = "ID услуги обязателен")
    private UUID serviceId;
}