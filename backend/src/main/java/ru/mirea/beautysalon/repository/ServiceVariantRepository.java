package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import ru.mirea.beautysalon.entity.ServiceVariant;

@Repository
public interface ServiceVariantRepository
        extends JpaRepository<ServiceVariant, UUID>, JpaSpecificationExecutor<ServiceVariant> {
    boolean existsByServiceId(UUID serviceId);
}