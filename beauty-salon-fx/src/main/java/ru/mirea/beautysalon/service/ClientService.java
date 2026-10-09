package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.MasterProfile;
import ru.mirea.beautysalon.repository.BookingRepository;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterProfileRepository;
import ru.mirea.beautysalon.repository.RoleRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


// Бизнес-правила для клиентов/мастеров: Имя и телефон обязательны.  Нельзя указать несуществующую роль.
// Нельзя сделать мастером того, кто уже мастер. Нельзя удалить клиента с активными записями.
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
        if (client.getPhone() == null || client.getPhone().isBlank()) {
            throw new BusinessException("Телефон клиента обязателен");
        }
        return clientRepository.update(client);
    }

    public void delete(UUID id) {
        getById(id);
        boolean hasActiveBookings = bookingRepository.findAll().stream()
                .anyMatch(b -> b.getClientId().equals(id)
                        && b.getStatus() != BookingStatus.COMPLETED
                        && b.getStatus() != BookingStatus.CANCELLED);
        if (hasActiveBookings) {
            throw new BusinessException("Нельзя удалить клиента с активными записями");
        }
        if (masterProfileRepository.existsById(id)) {
            masterProfileRepository.deleteById(id);
        }
        clientRepository.deleteById(id);
    }

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

    public boolean isMaster(UUID clientId) {
        return masterProfileRepository.existsById(clientId);
    }

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
}
