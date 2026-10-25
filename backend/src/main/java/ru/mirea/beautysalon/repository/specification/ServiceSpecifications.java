package ru.mirea.beautysalon.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import ru.mirea.beautysalon.entity.Service;

public class ServiceSpecifications {

    public static Specification<Service> filterServices(UUID masterId, UUID typeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (masterId != null) {
                predicates.add(cb.equal(root.get("master").get("id"), masterId));
            }
            if (typeId != null) {
                predicates.add(cb.equal(root.get("serviceType").get("id"), typeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}