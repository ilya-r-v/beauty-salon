package ru.mirea.beautysalon.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.client.ClientCreateRequest;
import ru.mirea.beautysalon.dto.client.ClientResponse;
import ru.mirea.beautysalon.dto.client.ClientUpdateRequest;
import ru.mirea.beautysalon.dto.client.MasterProfileResponse;
import ru.mirea.beautysalon.dto.client.PromoteRequest;
import ru.mirea.beautysalon.service.ClientService;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientResponse> getAllClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "name") String searchBy) {
        return clientService.getAllClients(search, searchBy);
    }

    @GetMapping("/{id}")
    public ClientResponse getClientById(@PathVariable UUID id) {
        return clientService.getClientById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse createClient(@Valid @RequestBody ClientCreateRequest request) {
        return clientService.createClient(request);
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientUpdateRequest request) {
        return clientService.updateClient(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
    }

    @PostMapping("/{id}/promote")
    @ResponseStatus(HttpStatus.CREATED)
    public MasterProfileResponse promoteToMaster(
            @PathVariable UUID id,
            @RequestBody(required = false) PromoteRequest request) {
        return clientService.promoteToMaster(id, request);
    }
}