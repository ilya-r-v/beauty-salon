package ru.mirea.beautysalon.service;
import ru.mirea.beautysalon.exception.BusinessException;
public final class PasswordPolicy {
    private PasswordPolicy() {}
    public static void validate(String value) {
        if (value == null || value.length() < 12 || value.length() > 72 || !value.matches(".*[A-Z].*") || !value.matches(".*[a-z].*") || !value.matches(".*[0-9].*"))
            throw new BusinessException("Пароль: 12–72 символа, прописные и строчные латинские буквы и цифры");
    }
}