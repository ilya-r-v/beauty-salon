package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.ServiceVariant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceVariantRepository {
    ServiceVariant save(ServiceVariant variant);
    List<ServiceVariant> findAll();
    Optional<ServiceVariant> findById(UUID id);
    ServiceVariant update(ServiceVariant variant);
    void deleteById(UUID id);
    List<ServiceVariant> findByService(UUID serviceId);
    List<ServiceVariant> filterByPriceRange(BigDecimal min, BigDecimal max);
    boolean existsById(UUID id);
}
