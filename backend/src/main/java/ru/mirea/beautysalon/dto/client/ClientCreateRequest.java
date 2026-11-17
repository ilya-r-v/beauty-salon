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
    @jakarta.validation.constraints.Size(max=100)
    private String name;

    @NotBlank(message = "Телефон обязателен для заполнения")
    @jakarta.validation.constraints.Pattern(regexp="[+]?[0-9]{7,19}")
    private String phone;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;
}