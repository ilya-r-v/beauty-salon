package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mirea.beautysalon.entity.Master;

@Repository
public interface MasterRepository extends JpaRepository<Master, UUID> {
}