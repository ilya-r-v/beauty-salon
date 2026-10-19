package ru.mirea.beautysalon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Имя обязательно")
    private String name;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен быть в формате +7XXXXXXXXXX")
    private String phone;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String passwordConfirm;

    public RegisterRequest() {}

    public RegisterRequest(String name, String phone, String password, String passwordConfirm) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
