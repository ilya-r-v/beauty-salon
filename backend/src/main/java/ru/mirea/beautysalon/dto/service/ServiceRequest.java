package ru.mirea.beautysalon.dto.service;

import java.util.UUID;

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
public class ServiceRequest {

    @NotBlank(message = "Название услуги не может быть пустым")
    private String title;

    @NotNull(message = "ID мастера обязателен")
    private UUID masterId;

    @NotNull(message = "ID типа услуги обязателен")
    private UUID serviceTypeId;
}