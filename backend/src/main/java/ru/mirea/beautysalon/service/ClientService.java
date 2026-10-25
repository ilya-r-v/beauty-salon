package ru.mirea.beautysalon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.client.ClientCreateRequest;
import ru.mirea.beautysalon.dto.client.ClientResponse;
import ru.mirea.beautysalon.dto.client.ClientUpdateRequest;
import ru.mirea.beautysalon.dto.client.MasterProfileResponse;
import ru.mirea.beautysalon.dto.client.PromoteRequest;
import ru.mirea.beautysalon.entity.BookingStatus;
import ru.mirea.beautysalon.entity.Client;
import ru.mirea.beautysalon.entity.Master;
import ru.mirea.beautysalon.entity.Role;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterRepository;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final MasterRepository masterRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients(String search, String searchBy) {
        List<Client> clients;
        if (search != null && !search.trim().isBlank()) {
            String trimmed = search.trim();
            if ("phone".equalsIgnoreCase(searchBy)) {
                clients = clientRepository.findByPhoneContaining(trimmed);
            } else {
                clients = clientRepository.findByNameContainingIgnoreCase(trimmed);
            }
        } else {
            clients = clientRepository.findAll();
        }

        return clients.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));
        return mapToResponse(client);
    }

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BusinessException("Имя не может быть пустым");
        }
        if (request.getPhone() == null || request.getPhone().trim().isBlank()) {
            throw new BusinessException("Телефон не может быть пустым");
        }

        String phone = request.getPhone().trim();
        if (clientRepository.existsByPhone(phone)) {
            throw new ConflictException("Пользователь с телефоном " + phone + " уже существует");
        }

        Client client = Client.builder()
                .name(request.getName().trim())
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .build();

        return mapToResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse updateClient(UUID id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));

        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BusinessException("Имя не может быть пустым");
        }
        if (request.getPhone() == null || request.getPhone().trim().isBlank()) {
            throw new BusinessException("Телефон не может быть пустым");
        }

        String phone = request.getPhone().trim();
        if (clientRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new ConflictException("Телефон " + phone + " уже используется другим клиентом");
        }

        client.setName(request.getName().trim());
        client.setPhone(phone);

        return mapToResponse(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));

        boolean hasActiveBookings = bookingRepository.existsByClientIdAndStatusIn(
                id,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
        if (hasActiveBookings) {
            throw new ConflictException("Нельзя удалить клиента с активными записями на прием");
        }

        clientRepository.delete(client);
    }

    @Transactional
    public MasterProfileResponse promoteToMaster(UUID id, PromoteRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));

        if (masterRepository.existsById(id)) {
            throw new ConflictException("Клиент уже является мастером");
        }

        client.setRole(Role.MASTER);
        clientRepository.save(client);

        Master master = Master.builder()
                .id(client.getId())
                .client(client)
                .description(request != null ? request.getDescription() : null)
                .rating(0.0f)
                .build();

        Master savedMaster = masterRepository.save(master);

        return MasterProfileResponse.builder()
                .clientId(savedMaster.getId())
                .name(client.getName())
                .avatarUrl(savedMaster.getAvatarUrl())
                .description(savedMaster.getDescription())
                .rating(savedMaster.getRating())
                .build();
    }

    private ClientResponse mapToResponse(Client client) {
        boolean isMaster = masterRepository.existsById(client.getId());
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .role(client.getRole().toString())
                .isMaster(isMaster)
                .build();
    }
}