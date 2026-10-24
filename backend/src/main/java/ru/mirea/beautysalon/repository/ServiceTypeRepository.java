package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.ServiceType;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {
    boolean existsByNameIgnoreCase(String name);
}