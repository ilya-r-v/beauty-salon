package ru.mirea.beautysalon.service;

import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.ServiceEntity;
import ru.mirea.beautysalon.model.ServiceType;
import ru.mirea.beautysalon.model.ServiceVariant;
import ru.mirea.beautysalon.repository.MasterProfileRepository;
import ru.mirea.beautysalon.repository.ServiceEntityRepository;
import ru.mirea.beautysalon.repository.ServiceTypeRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


// Бизнес-правила для каталога услуг Нельзя создать услугу с несуществующим мастером. Нельзя создать услугу с несуществующим типом.
// Нельзя создать вариант услуги с ценой <= 0. Нельзя создать вариант услуги для несуществующей услуги.
public class ServiceCatalogService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final ServiceVariantRepository serviceVariantRepository;
    private final MasterProfileRepository masterProfileRepository;

    public ServiceCatalogService(ServiceTypeRepository serviceTypeRepository,
                                  ServiceEntityRepository serviceEntityRepository,
                                  ServiceVariantRepository serviceVariantRepository,
                                  MasterProfileRepository masterProfileRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceEntityRepository = serviceEntityRepository;
        this.serviceVariantRepository = serviceVariantRepository;
        this.masterProfileRepository = masterProfileRepository;
    }

    public ServiceType createType(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Название типа услуги обязательно");
        }
        return serviceTypeRepository.save(new ServiceType(null, name));
    }

    public List<ServiceType> findAllTypes() {
        return serviceTypeRepository.findAll();
    }

    public ServiceEntity createService(ServiceEntity service) {
        if (service.getTitle() == null || service.getTitle().isBlank()) {
            throw new BusinessException("Название услуги обязательно");
        }
        if (service.getMasterId() == null || !masterProfileRepository.existsById(service.getMasterId())) {
            throw new BusinessException("Указан несуществующий мастер");
        }
        if (service.getServiceTypeId() == null || !serviceTypeRepository.existsById(service.getServiceTypeId())) {
            throw new BusinessException("Указан несуществующий тип услуги");
        }
        return serviceEntityRepository.save(service);
    }

    public List<ServiceEntity> findAllServices() {
        return serviceEntityRepository.findAll();
    }

    public ServiceEntity getServiceById(UUID id) {
        return serviceEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с id=" + id + " не найдена"));
    }

    public ServiceEntity updateService(ServiceEntity service) {
        getServiceById(service.getId());
        if (service.getTitle() == null || service.getTitle().isBlank()) {
            throw new BusinessException("Название услуги обязательно");
        }
        return serviceEntityRepository.update(service);
    }

    public void deleteService(UUID id) {
        getServiceById(id);
        if (!serviceVariantRepository.findByService(id).isEmpty()) {
            throw new BusinessException("Нельзя удалить услугу, у которой есть варианты — сначала удалите их");
        }
        serviceEntityRepository.deleteById(id);
    }

    public List<ServiceEntity> filterByMaster(UUID masterId) {
        return serviceEntityRepository.findByMaster(masterId);
    }

    public List<ServiceEntity> filterByType(UUID typeId) {
        return serviceEntityRepository.findByType(typeId);
    }

    public List<ServiceEntity> searchByTitle(String titlePart) {
        String normalizedTitlePart = titlePart.toLowerCase();
        List<ServiceEntity> result = new ArrayList<>();
        for (ServiceEntity service : serviceEntityRepository.findAll()) {
            if (service.getTitle().toLowerCase().contains(normalizedTitlePart)) {
                result.add(service);
            }
        }
        return result;
    }

    public ServiceVariant createVariant(ServiceVariant variant) {
        if (variant.getTitle() == null || variant.getTitle().isBlank()) {
            throw new BusinessException("Название варианта услуги обязательно");
        }
        if (variant.getPrice() == null || variant.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Цена должна быть больше нуля");
        }
        if (variant.getServiceId() == null || !serviceEntityRepository.existsById(variant.getServiceId())) {
            throw new BusinessException("Указана несуществующая услуга");
        }
        return serviceVariantRepository.save(variant);
    }

    public List<ServiceVariant> findAllVariants() {
        return serviceVariantRepository.findAll();
    }

    public ServiceVariant getVariantById(UUID id) {
        return serviceVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Вариант услуги с id=" + id + " не найден"));
    }

    public void deleteVariant(UUID id) {
        getVariantById(id);
        serviceVariantRepository.deleteById(id);
    }

    public List<ServiceVariant> filterByPriceRange(BigDecimal min, BigDecimal max) {
        return serviceVariantRepository.filterByPriceRange(min, max);
    }

    public List<ServiceVariant> sortByPrice(List<ServiceVariant> variants) {
        List<ServiceVariant> result = new ArrayList<>(variants);
        result.sort(Comparator.comparing(ServiceVariant::getPrice));
        return result;
    }

    public List<ServiceVariant> sortByTitle(List<ServiceVariant> variants) {
        List<ServiceVariant> result = new ArrayList<>(variants);
        result.sort(Comparator.comparing(ServiceVariant::getTitle));
        return result;
    }
}
