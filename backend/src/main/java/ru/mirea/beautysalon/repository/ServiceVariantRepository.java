package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.beautysalon.entity.ServiceVariant;

public interface ServiceVariantRepository extends JpaRepository<ServiceVariant, UUID> {
    boolean existsByServiceId(UUID serviceId);
}
