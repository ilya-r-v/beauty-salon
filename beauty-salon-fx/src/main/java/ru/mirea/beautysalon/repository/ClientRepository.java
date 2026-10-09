package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Client save(Client client);
    List<Client> findAll();
    Optional<Client> findById(UUID id);
    Client update(Client client);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
