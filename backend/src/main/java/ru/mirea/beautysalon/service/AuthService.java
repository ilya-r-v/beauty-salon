package ru.mirea.beautysalon.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.domain.Client;
import ru.mirea.beautysalon.domain.Role;
import ru.mirea.beautysalon.dto.auth.AuthResponse;
import ru.mirea.beautysalon.dto.auth.LoginRequest;
import ru.mirea.beautysalon.dto.auth.MeResponse;
import ru.mirea.beautysalon.dto.auth.RegisterRequest;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterRepository;
import ru.mirea.beautysalon.security.JwtTokenProvider;
import ru.mirea.beautysalon.security.PasswordPolicy;
import ru.mirea.beautysalon.security.RefreshSessionService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final MasterRepository masterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshSessionService sessions;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        PasswordPolicy.validate(request.getPassword());
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

        Client savedClient = clientRepository.saveAndFlush(client);
        String token = tokenProvider.generateToken(savedClient);

        return AuthResponse.builder()
            .token(token)
            .refreshToken(sessions.issue(savedClient.getId()))
            .passwordChangeRequired(savedClient.isPasswordChangeRequired())
            .id(savedClient.getId())
            .name(savedClient.getName())
            .phone(savedClient.getPhone())
            .role(savedClient.getRole().toString())
            .build();
    }

    @Transactional(noRollbackFor = org.springframework.web.server.ResponseStatusException.class)
    public AuthResponse login(LoginRequest request) {
        String phone = request.getPhone().strip();
        jdbc.update("delete from login_attempt where since < now() - interval '1 day'");
        boolean throttled = Boolean.TRUE.equals(jdbc.queryForObject("""
            insert into login_attempt(phone, failures, since) values (?, 0, clock_timestamp())
            on conflict (phone) do update set failures = login_attempt.failures
            returning failures >= 5 and since > clock_timestamp() - interval '1 second'
            """, Boolean.class, phone));
        if (throttled) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
        );
        Client client = clientRepository.findByPhone(phone).orElse(null);
        if (
            client == null ||
            !client.isActive() ||
            !passwordEncoder.matches(request.getPassword(), client.getPasswordHash())
        ) {
            jdbc.update("update login_attempt set failures=least(failures+1,5),since=clock_timestamp() where phone=?", phone);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED
            );
        }
        jdbc.update("delete from login_attempt where phone=?", phone);
        jdbc.update(
            "insert into audit_event(actor,event,entity,entity_id,description,correlation_id) values(?,'LOGIN','client',?,'Вход',current_setting('salon.correlation',true))",
            client.getId(),
            client.getId()
        );

        String token = tokenProvider.generateToken(client);

        return AuthResponse.builder()
            .token(token)
            .refreshToken(sessions.issue(client.getId()))
            .passwordChangeRequired(client.isPasswordChangeRequired())
            .id(client.getId())
            .name(client.getName())
            .phone(client.getPhone())
            .role(client.getRole().toString())
            .build();
    }

    @Transactional
    public void changePassword(UUID id, String current, String next) {
        var client = clientRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        if (!passwordEncoder.matches(current, client.getPasswordHash())) throw new BusinessException(
            "Текущий пароль неверен"
        );
        PasswordPolicy.validate(next);
        if (current.equals(next)) throw new BusinessException("Новый пароль должен отличаться");
        client.setCredentialVersion(client.getCredentialVersion() + 1);
        client.setPasswordHash(passwordEncoder.encode(next));
        client.setPasswordChangeRequired(false);
        jdbc.update("DELETE FROM refresh_session WHERE client_id=?", id);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID currentUserId) {
        Client client = clientRepository
            .findById(currentUserId)
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
