package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}