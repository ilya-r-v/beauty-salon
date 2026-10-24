package ru.mirea.beautysalon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);

    List<Client> findByNameContainingIgnoreCase(String name);

    List<Client> findByPhoneContaining(String phone);

    long countByRole(String role);
}