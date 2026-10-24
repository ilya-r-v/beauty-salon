package ru.mirea.beautysalon.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.service.ServiceTypeRequest;
import ru.mirea.beautysalon.dto.service.ServiceTypeResponse;
import ru.mirea.beautysalon.entity.ServiceType;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.repository.ServiceTypeRepository;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> getAllServiceTypes() {
        return serviceTypeRepository.findAll()
                .stream()
                .map(type -> ServiceTypeResponse.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .build())
                .toList();
    }

    @Transactional
    public ServiceTypeResponse createServiceType(ServiceTypeRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BusinessException("Название типа услуги не может быть пустым");
        }

        String trimmedName = request.getName().trim();

        if (serviceTypeRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new ConflictException("Тип услуги с названием '" + trimmedName + "' уже существует");
        }

        ServiceType serviceType = ServiceType.builder()
                .name(trimmedName)
                .build();

        ServiceType saved = serviceTypeRepository.save(serviceType);

        return ServiceTypeResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }
}