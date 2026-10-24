package ru.mirea.beautysalon.repository.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import ru.mirea.beautysalon.entity.ServiceVariant;

public class ServiceVariantSpecifications {

    public static Specification<ServiceVariant> filterVariants(
            UUID serviceId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (serviceId != null) {
                predicates.add(cb.equal(root.get("service").get("id"), serviceId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}