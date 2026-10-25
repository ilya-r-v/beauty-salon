package ru.mirea.beautysalon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ru.mirea.beautysalon.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {
}