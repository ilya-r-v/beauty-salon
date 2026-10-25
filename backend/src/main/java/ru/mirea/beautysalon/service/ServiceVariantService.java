package ru.mirea.beautysalon.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.service.VariantRequest;
import ru.mirea.beautysalon.dto.service.VariantResponse;
import ru.mirea.beautysalon.entity.ServiceVariant;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.ServiceRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;
import ru.mirea.beautysalon.repository.specification.ServiceVariantSpecifications;

@Service
@RequiredArgsConstructor
public class ServiceVariantService {

    private final ServiceVariantRepository serviceVariantRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<VariantResponse> getAllVariants(UUID serviceId, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<ServiceVariant> spec = ServiceVariantSpecifications.filterVariants(serviceId, minPrice, maxPrice);

        return serviceVariantRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VariantResponse getVariantById(UUID id) {
        ServiceVariant variant = serviceVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вариант услуги с ID " + id + " не найден"));
        return mapToResponse(variant);
    }

    @Transactional
    public VariantResponse createVariant(VariantRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isBlank()) {
            throw new BusinessException("Название варианта услуги не может быть пустым");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Цена должна быть строго больше нуля");
        }

        ru.mirea.beautysalon.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Услуга с ID " + request.getServiceId() + " не найдена"));

        ServiceVariant variant = ServiceVariant.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .service(service)
                .build();

        return mapToResponse(serviceVariantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(UUID id) {
        ServiceVariant variant = serviceVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вариант услуги с ID " + id + " не найден"));

        serviceVariantRepository.delete(variant);
    }

    private VariantResponse mapToResponse(ServiceVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .title(variant.getTitle())
                .description(variant.getDescription())
                .price(variant.getPrice())
                .serviceId(variant.getService().getId())
                .serviceTitle(variant.getService().getTitle())
                .build();
    }
}