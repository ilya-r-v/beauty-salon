package ru.mirea.beautysalon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.master.MasterResponse;
import ru.mirea.beautysalon.entity.Master;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.repository.MasterRepository;

@Service
@RequiredArgsConstructor
public class MasterService {

    private final MasterRepository masterRepository;

    @Transactional(readOnly = true)
    public List<MasterResponse> getAllMasters() {
        return masterRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MasterResponse getMasterById(UUID clientId) {
        Master master = masterRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Мастер с ID " + clientId + " не найден"));
        return mapToResponse(master);
    }

    private MasterResponse mapToResponse(Master master) {
        return MasterResponse.builder()
                .clientId(master.getId())
                .name(master.getClient().getName())
                .phone(master.getClient().getPhone())
                .avatarUrl(master.getAvatarUrl())
                .description(master.getDescription())
                .rating(master.getRating() != null ? master.getRating() : 0.0)
                .build();
    }
}