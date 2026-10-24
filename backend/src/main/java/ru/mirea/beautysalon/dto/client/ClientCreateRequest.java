package ru.mirea.beautysalon.dto.client;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    private String name;

    @NotBlank(message = "Телефон обязателен для заполнения")
    private String phone;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;
}