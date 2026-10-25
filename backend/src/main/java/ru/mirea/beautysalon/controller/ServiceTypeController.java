package ru.mirea.beautysalon.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.service.ServiceTypeRequest;
import ru.mirea.beautysalon.dto.service.ServiceTypeResponse;
import ru.mirea.beautysalon.service.ServiceTypeService;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public List<ServiceTypeResponse> getAllServiceTypes() {
        return serviceTypeService.getAllServiceTypes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceTypeResponse createServiceType(@Valid @RequestBody ServiceTypeRequest request) {
        return serviceTypeService.createServiceType(request);
    }
}