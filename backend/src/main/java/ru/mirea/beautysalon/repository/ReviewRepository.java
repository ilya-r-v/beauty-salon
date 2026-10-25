package ru.mirea.beautysalon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mirea.beautysalon.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByMasterId(UUID masterId);
}