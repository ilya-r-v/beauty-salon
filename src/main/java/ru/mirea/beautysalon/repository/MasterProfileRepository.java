package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.MasterProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MasterProfileRepository {
    MasterProfile save(MasterProfile profile);
    List<MasterProfile> findAll();
    Optional<MasterProfile> findById(UUID clientId);
    MasterProfile update(MasterProfile profile);
    void deleteById(UUID clientId);
    boolean existsById(UUID clientId);
}
