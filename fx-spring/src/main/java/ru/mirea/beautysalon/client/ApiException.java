package ru.mirea.beautysalon.client;

// Бросается ApiClient-ом при HTTP-ошибках (4xx, 5xx).
// Контроллеры ловят и показывают e.getMessage() пользователю.
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isConflict() {
        return statusCode == 409;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }
}

