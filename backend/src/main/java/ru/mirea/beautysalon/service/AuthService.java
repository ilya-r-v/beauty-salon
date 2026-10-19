package ru.mirea.beautysalon.service;

import org.springframework.stereotype.Service;
import ru.mirea.beautysalon.config.JwtTokenProvider;
import ru.mirea.beautysalon.dto.AuthResponse;
import ru.mirea.beautysalon.dto.LoginRequest;
import ru.mirea.beautysalon.dto.RegisterRequest;
import ru.mirea.beautysalon.dto.UserResponse;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.Role;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.RoleRepository;
import ru.mirea.beautysalon.util.PasswordHasher;

import java.util.regex.Pattern;

@Service
public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");
    private static final String DEFAULT_ROLE = "CLIENT";
    private static final String LOGIN_ERROR = "Неверный телефон или пароль";

    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(ClientRepository clientRepository,
                       RoleRepository roleRepository,
                       JwtTokenProvider jwtTokenProvider) {
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest req) {
        String cleanName = req.getName() == null ? "" : req.getName().trim();
        String cleanPhone = req.getPhone() == null ? "" : req.getPhone().trim();
        String password = req.getPassword();
        String passwordConfirm = req.getPasswordConfirm();

        if (cleanName.isEmpty()) {
            throw new BusinessException("Имя обязательно");
        }
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new BusinessException("Телефон должен быть в формате +7XXXXXXXXXX");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Пароль должен быть не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException("Пароли не совпадают");
        }
        if (clientRepository.findByPhone(cleanPhone).isPresent()) {
            throw new BusinessException("Пользователь с таким телефоном уже зарегистрирован");
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new BusinessException("В БД нет роли " + DEFAULT_ROLE));

        Client client = new Client(null, cleanName, cleanPhone, PasswordHasher.hash(password), role.getId());
        Client saved = clientRepository.save(client);

        String token = jwtTokenProvider.generateToken(saved, DEFAULT_ROLE);
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getPhone(), DEFAULT_ROLE);
    }

    public AuthResponse login(LoginRequest req) {
        String cleanPhone = req.getPhone() == null ? "" : req.getPhone().trim();
        String password = req.getPassword();

        if (cleanPhone.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("Введите телефон и пароль");
        }

        Client client = clientRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new BusinessException(LOGIN_ERROR));

        if (!PasswordHasher.verify(password, client.getPasswordHash())) {
            throw new BusinessException(LOGIN_ERROR);
        }

        String roleName = roleRepository.findById(client.getRoleId())
                .map(Role::getName)
                .orElse(DEFAULT_ROLE);

        String token = jwtTokenProvider.generateToken(client, roleName);
        return new AuthResponse(token, client.getId(), client.getName(), client.getPhone(), roleName);
    }

    public UserResponse getProfile(String phone) {
        String cleanPhone = phone == null ? "" : phone.trim();
        Client client = clientRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        String roleName = roleRepository.findById(client.getRoleId())
                .map(Role::getName)
                .orElse(DEFAULT_ROLE);

        return new UserResponse(client.getId(), client.getName(), client.getPhone(), roleName);
    }
}
