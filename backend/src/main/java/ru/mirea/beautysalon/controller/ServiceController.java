package ru.mirea.beautysalon.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.service.ServiceRequest;
import ru.mirea.beautysalon.dto.service.ServiceResponse;
import ru.mirea.beautysalon.service.ServiceEntityService;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceEntityService serviceEntityService;

    @GetMapping
    public List<ServiceResponse> getAllServices(
            @RequestParam(required = false) UUID masterId,
            @RequestParam(required = false) UUID typeId) {
        return serviceEntityService.getAllServices(masterId, typeId);
    }

    @GetMapping("/{id}")
    public ServiceResponse getServiceById(@PathVariable UUID id) {
        return serviceEntityService.getServiceById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody ServiceRequest request) {
        return serviceEntityService.createService(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable UUID id) {
        serviceEntityService.deleteService(id);
    }
}