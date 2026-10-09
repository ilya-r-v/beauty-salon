package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.ServiceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceEntityRepository {
    ServiceEntity save(ServiceEntity service);
    List<ServiceEntity> findAll();
    Optional<ServiceEntity> findById(UUID id);
    ServiceEntity update(ServiceEntity service);
    void deleteById(UUID id);
    List<ServiceEntity> findByMaster(UUID masterId);
    List<ServiceEntity> findByType(UUID serviceTypeId);
    boolean existsById(UUID id);
}
