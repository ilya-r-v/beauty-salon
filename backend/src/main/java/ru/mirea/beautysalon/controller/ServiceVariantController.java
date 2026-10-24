package ru.mirea.beautysalon.controller;

import java.math.BigDecimal;
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
import ru.mirea.beautysalon.dto.service.VariantRequest;
import ru.mirea.beautysalon.dto.service.VariantResponse;
import ru.mirea.beautysalon.service.ServiceVariantService;

@RestController
@RequestMapping("/api/service-variants")
@RequiredArgsConstructor
public class ServiceVariantController {

    private final ServiceVariantService serviceVariantService;

    @GetMapping
    public List<VariantResponse> getAllVariants(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return serviceVariantService.getAllVariants(serviceId, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public VariantResponse getVariantById(@PathVariable UUID id) {
        return serviceVariantService.getVariantById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VariantResponse createVariant(@Valid @RequestBody VariantRequest request) {
        return serviceVariantService.createVariant(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVariant(@PathVariable UUID id) {
        serviceVariantService.deleteVariant(id);
    }
}