package ru.mirea.beautysalon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.service.ServiceRequest;
import ru.mirea.beautysalon.dto.service.ServiceResponse;
import ru.mirea.beautysalon.entity.Master;
import ru.mirea.beautysalon.entity.ServiceType;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.ConflictException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.MasterRepository;
import ru.mirea.beautysalon.repository.ServiceRepository;
import ru.mirea.beautysalon.repository.ServiceTypeRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;
import ru.mirea.beautysalon.repository.specification.ServiceSpecifications;

@Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final ServiceVariantRepository serviceVariantRepository;
    private final MasterRepository masterRepository;
    private final ServiceTypeRepository serviceTypeRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices(UUID masterId, UUID typeId) {
        Specification<ru.mirea.beautysalon.entity.Service> spec = ServiceSpecifications.filterServices(masterId,
                typeId);

        return serviceRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID id) {
        ru.mirea.beautysalon.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));
        return mapToResponse(service);
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isBlank()) {
            throw new BusinessException("Название услуги не может быть пустым");
        }

        Master master = masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new EntityNotFoundException("Мастер с ID " + request.getMasterId() + " не найден"));

        ServiceType serviceType = serviceTypeRepository.findById(request.getServiceTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Тип услуги с ID " + request.getServiceTypeId() + " не найден"));

        ru.mirea.beautysalon.entity.Service service = ru.mirea.beautysalon.entity.Service.builder()
                .title(request.getTitle().trim())
                .master(master)
                .serviceType(serviceType)
                .build();

        return mapToResponse(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(UUID id) {
        ru.mirea.beautysalon.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));

        if (serviceVariantRepository.existsByServiceId(id)) {
            throw new ConflictException("Нельзя удалить услугу, у которой есть варианты с ценами");
        }

        serviceRepository.delete(service);
    }

    private ServiceResponse mapToResponse(ru.mirea.beautysalon.entity.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .title(service.getTitle())
                .masterId(service.getMaster().getId())
                .masterName(service.getMaster().getClient().getName())
                .serviceTypeId(service.getServiceType().getId())
                .serviceTypeName(service.getServiceType().getName())
                .build();
    }
}