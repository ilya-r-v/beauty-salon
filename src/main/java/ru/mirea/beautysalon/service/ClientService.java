package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.MasterProfile;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterProfileRepository;
import ru.mirea.beautysalon.repository.RoleRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


// Бизнес-правила для клиентов/мастеров Нельзя создать клиента без имени или телефона. Нельзя указать несуществующую роль.
// Нельзя сделать мастером того, кто уже мастер. Нельзя удалить клиента, у которого есть активные (не завершённые/не отменённые) записи.

public class ClientService {

    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final MasterProfileRepository masterProfileRepository;
    private final BookingRepository bookingRepository;

    public ClientService(ClientRepository clientRepository,
                          RoleRepository roleRepository,
                          MasterProfileRepository masterProfileRepository,
                          BookingRepository bookingRepository) {
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.masterProfileRepository = masterProfileRepository;
        this.bookingRepository = bookingRepository;
    }

    public Client create(Client client) {
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("Имя клиента обязательно");
        }
        if (client.getPhone() == null || client.getPhone().isBlank()) {
            throw new BusinessException("Телефон клиента обязателен");
        }
        if (client.getRoleId() == null || roleRepository.findById(client.getRoleId()).isEmpty()) {
            throw new BusinessException("Указана несуществующая роль");
        }
        return clientRepository.save(client);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client getById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с id=" + id + " не найден"));
    }

    public Client update(Client client) {
        getById(client.getId());
        if (client.getName() == null || client.getName().isBlank()) {
            throw new BusinessException("Имя клиента обязательно");
        }
        return clientRepository.update(client);
    }

    public void delete(UUID id) {
        getById(id);
        boolean hasActiveBookings = bookingRepository.findAll().stream()
                .anyMatch(b -> b.getClientId().equals(id)
                        && b.getStatus() != ru.mirea.beautysalon.model.BookingStatus.COMPLETED
                        && b.getStatus() != ru.mirea.beautysalon.model.BookingStatus.CANCELLED);
        if (hasActiveBookings) {
            throw new BusinessException("Нельзя удалить клиента с активными записями");
        }
        if (masterProfileRepository.existsById(id)) {
            masterProfileRepository.deleteById(id);
        }
        clientRepository.deleteById(id);
    }

// Делает существующего клиента мастером — создаёт master_profile.
    public MasterProfile promoteToMaster(UUID clientId, String description) {
        getById(clientId);
        if (masterProfileRepository.existsById(clientId)) {
            throw new BusinessException("Клиент уже является мастером");
        }
        MasterProfile profile = new MasterProfile(clientId, null, description, 0f);
        return masterProfileRepository.save(profile);
    }

    public List<MasterProfile> findAllMasters() {
        return masterProfileRepository.findAll();
    }

    // поиск
    public List<Client> searchByName(String namePart) {
        return clientRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(namePart.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Client> searchByPhone(String phonePart) {
        return clientRepository.findAll().stream()
                .filter(c -> c.getPhone().contains(phonePart))
                .collect(Collectors.toList());
    }

    // сортировка
    public List<Client> sortByName(List<Client> clients) {
        return clients.stream()
                .sorted(Comparator.comparing(Client::getName))
                .collect(Collectors.toList());
    }
}