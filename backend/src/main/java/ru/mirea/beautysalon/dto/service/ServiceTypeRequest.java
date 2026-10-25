package ru.mirea.beautysalon.dto.service;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeRequest {

    @NotBlank(message = "Название типа услуги не может быть пустым")
    private String name;
}