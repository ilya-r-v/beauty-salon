package ru.mirea.beautysalon.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.config.JwtTokenProvider;
import ru.mirea.beautysalon.dto.auth.AuthResponse;
import ru.mirea.beautysalon.dto.auth.LoginRequest;
import ru.mirea.beautysalon.dto.auth.MeResponse;
import ru.mirea.beautysalon.dto.auth.RegisterRequest;
import ru.mirea.beautysalon.entity.Client;
import ru.mirea.beautysalon.entity.Role;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final MasterRepository masterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException("Пароли не совпадают");
        }

        String phone = request.getPhone().trim();
        if (clientRepository.existsByPhone(phone)) {
            throw new ConflictException("Пользователь с таким номером телефона уже зарегистрирован");
        }

        Client client = Client.builder()
                .name(request.getName().trim())
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .build();

        Client savedClient = clientRepository.save(client);
        String token = tokenProvider.generateToken(savedClient);

        return AuthResponse.builder()
                .token(token)
                .id(savedClient.getId())
                .name(savedClient.getName())
                .phone(savedClient.getPhone())
                .role(savedClient.getRole().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Client client = clientRepository.findByPhone(request.getPhone().trim())
                .orElseThrow(() -> new BusinessException("Неверный номер телефона или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), client.getPasswordHash())) {
            throw new BusinessException("Неверный номер телефона или пароль");
        }

        String token = tokenProvider.generateToken(client);

        return AuthResponse.builder()
                .token(token)
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .role(client.getRole().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID currentUserId) {
        Client client = clientRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        boolean isMaster = masterRepository.existsById(currentUserId);

        return MeResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .role(client.getRole().toString())
                .isMaster(isMaster)
                .build();
    }
}