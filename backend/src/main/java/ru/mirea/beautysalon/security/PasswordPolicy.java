package ru.mirea.beautysalon.security;

import java.nio.charset.StandardCharsets;
import ru.mirea.beautysalon.exception.BusinessException;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validate(String value) {
        if (value == null || value.isBlank()) throw new BusinessException("Укажите пароль");
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Пароль превышает предел BCrypt в 72 байта");
        }
    }
}
