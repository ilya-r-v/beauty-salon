package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    List<Role> findAll();
    Optional<Role> findById(UUID id);
    Optional<Role> findByName(String name);
}
