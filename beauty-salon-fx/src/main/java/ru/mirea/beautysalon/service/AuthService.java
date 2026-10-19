package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.Role;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.RoleRepository;
import ru.mirea.beautysalon.util.PasswordHasher;

import java.util.Optional;
import java.util.regex.Pattern;

// Регистрация и вход пользователей через JDBC.
public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");
    private static final String DEFAULT_ROLE = "CLIENT";
    private static final String LOGIN_ERROR = "Неверный телефон или пароль";

    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;

    public AuthService(ClientRepository clientRepository, RoleRepository roleRepository) {
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
    }

    public Client register(String name, String phone, String password, String passwordConfirm) {
        String cleanName = name == null ? "" : name.trim();
        String cleanPhone = phone == null ? "" : phone.trim();

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
        return clientRepository.save(client);
    }

    public Client login(String phone, String password) {
        String cleanPhone = phone == null ? "" : phone.trim();
        if (cleanPhone.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("Введите телефон и пароль");
        }
        Optional<Client> client = clientRepository.findByPhone(cleanPhone);
        if (client.isEmpty() || !PasswordHasher.verify(password, client.get().getPasswordHash())) {
            throw new BusinessException(LOGIN_ERROR);
        }
        return client.get();
    }

    public String roleName(Client client) {
        return roleRepository.findById(client.getRoleId()).map(Role::getName).orElse("—");
    }
}
