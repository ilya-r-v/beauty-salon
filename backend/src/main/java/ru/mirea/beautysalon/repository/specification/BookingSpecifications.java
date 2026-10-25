package ru.mirea.beautysalon.repository.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import ru.mirea.beautysalon.entity.Booking;
import ru.mirea.beautysalon.entity.BookingStatus;

public class BookingSpecifications {

    public static Specification<Booking> filterBookings(
            String search,
            String searchBy,
            BookingStatus status,
            UUID masterId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (masterId != null) {
                predicates.add(cb.equal(root.get("timeSlot").get("master").get("id"), masterId));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeSlot").get("startTime"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeSlot").get("endTime"), dateTo));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                if ("serviceTitle".equalsIgnoreCase(searchBy)) {
                    predicates.add(cb.like(cb.lower(root.get("serviceVariant").get("title")), pattern));
                } else {
                    predicates.add(cb.like(cb.lower(root.get("client").get("name")), pattern));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}