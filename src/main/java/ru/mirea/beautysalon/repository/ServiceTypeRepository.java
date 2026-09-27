package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.ServiceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceTypeRepository {
    ServiceType save(ServiceType type);
    List<ServiceType> findAll();
    Optional<ServiceType> findById(UUID id);
    boolean existsById(UUID id);
}
