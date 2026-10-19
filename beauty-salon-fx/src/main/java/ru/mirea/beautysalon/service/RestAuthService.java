package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.client.AuthRestClient;
import ru.mirea.beautysalon.client.dto.AuthResponseDto;
import ru.mirea.beautysalon.fx.Session;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.RoleRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация авторизации через Spring Boot REST API.
 * Написана рядом с JDBC AuthService и совместима с ним.
 */
public class RestAuthService extends AuthService {

    private final AuthRestClient restClient;
    private final Map<UUID, String> roleCache = new ConcurrentHashMap<>();

    public RestAuthService(AuthRestClient restClient) {
        super(null, null);
        this.restClient = restClient;
    }

    public RestAuthService(AuthRestClient restClient, ClientRepository clientRepository, RoleRepository roleRepository) {
        super(clientRepository, roleRepository);
        this.restClient = restClient;
    }

    @Override
    public Client register(String name, String phone, String password, String passwordConfirm) {
        AuthResponseDto dto = restClient.register(name, phone, password, passwordConfirm);
        Client client = new Client(dto.getId(), dto.getName(), dto.getPhone(), "", null);
        if (dto.getRole() != null) {
            roleCache.put(dto.getId(), dto.getRole());
        }
        Session.setToken(dto.getToken());
        return client;
    }

    @Override
    public Client login(String phone, String password) {
        AuthResponseDto dto = restClient.login(phone, password);
        Client client = new Client(dto.getId(), dto.getName(), dto.getPhone(), "", null);
        if (dto.getRole() != null) {
            roleCache.put(dto.getId(), dto.getRole());
        }
        Session.setToken(dto.getToken());
        return client;
    }

    @Override
    public String roleName(Client client) {
        if (client == null || client.getId() == null) {
            return "—";
        }
        String cached = roleCache.get(client.getId());
        if (cached != null) {
            return cached;
        }
        try {
            return super.roleName(client);
        } catch (Exception e) {
            return "CLIENT";
        }
    }

    public AuthRestClient getRestClient() {
        return restClient;
    }
}
