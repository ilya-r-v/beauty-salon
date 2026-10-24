package ru.mirea.beautysalon.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.auth.AuthResponse;
import ru.mirea.beautysalon.dto.auth.LoginRequest;
import ru.mirea.beautysalon.dto.auth.MeResponse;
import ru.mirea.beautysalon.dto.auth.RegisterRequest;
import ru.mirea.beautysalon.exception.ForbiddenException;
import ru.mirea.beautysalon.service.AuthService;

@Tag(name = "Аутентификация", description = "Регистрация, авторизация и получение текущего профиля")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация нового клиента")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Вход в систему")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Данные текущего пользователя")
    @GetMapping("/me")
    public MeResponse getMe(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID currentUserId)) {
            throw new ForbiddenException("Пользователь не авторизован");
        }
        return authService.getMe(currentUserId);
    }
}