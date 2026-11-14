package ru.mirea.beautysalon.error;

import java.time.Instant;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.mirea.beautysalon.exception.*;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception ex, HttpServletRequest request) {
        int status = 500;
        String message = "Внутренняя ошибка сервера";
        if (ex instanceof EntityNotFoundException) status = 404;
        else if (ex instanceof ForbiddenException) status = 403;
        else if (ex instanceof ConflictException || ex instanceof ObjectOptimisticLockingFailureException || ex instanceof DataIntegrityViolationException) status = 409;
        else if (ex instanceof BusinessException || ex instanceof MethodArgumentNotValidException || ex instanceof HttpMessageNotReadableException || ex instanceof MethodArgumentTypeMismatchException || ex instanceof IllegalArgumentException) status = 400;
        else if (ex instanceof ResponseStatusException r) status = r.getStatusCode().value();
        if (ex instanceof BusinessException || ex instanceof EntityNotFoundException || ex instanceof ForbiddenException || ex instanceof ConflictException) message = ex.getMessage();
        else if (status == 400) message = "Некорректные данные запроса";
        else if (status == 409) message = "Конфликт данных или версии записи";
        else if (status == 401) message = "Требуется авторизация";
        return ResponseEntity.status(status).body(Map.of("status", status, "message", message, "timestamp", Instant.now().toString(), "path", request.getRequestURI(), "correlationId", String.valueOf(request.getAttribute("correlationId"))));
    }
}