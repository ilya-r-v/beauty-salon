package ru.mirea.beautysalon.filter;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.mirea.beautysalon.domain.Booking;
import ru.mirea.beautysalon.domain.BookingStatus;

public record BookingFilter(
    String search,
    UUID typeId,
    UUID masterId,
    BookingStatus status,
    Boolean prepaid,
    String priority,
    String tag,
    Boolean archived,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    String sortBy,
    String direction,
    OffsetDateTime dateFrom,
    OffsetDateTime dateTo
) {
    public Specification<Booking> specification() {
        return (root, query, cb) -> {
            var terms = new ArrayList<Predicate>();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.strip().toLowerCase(java.util.Locale.ROOT) + "%";
                terms.add(
                    cb.or(
                        cb.like(cb.lower(root.get("client").get("name")), term),
                        cb.like(cb.lower(root.get("serviceVariant").get("title")), term),
                        cb.like(cb.lower(root.get("comment")), term),
                        cb.like(cb.lower(root.get("externalCode")), term)
                    )
                );
            }
            if (typeId != null) terms.add(
                cb.equal(root.get("serviceVariant").get("service").get("serviceType").get("id"), typeId)
            );
            if (masterId != null) terms.add(cb.equal(root.get("timeSlot").get("master").get("id"), masterId));
            if (status != null) terms.add(cb.equal(root.get("status"), status));
            if (prepaid != null) terms.add(cb.equal(root.get("prepaid"), prepaid));
            if (priority != null) terms.add(cb.equal(root.get("priority"), priority));
            if (tag != null) terms.add(cb.isMember(tag, root.get("tags")));
            if (archived != null) terms.add(cb.equal(root.get("archived"), archived));
            if (minPrice != null) terms.add(cb.ge(root.get("bookedPrice"), minPrice));
            if (maxPrice != null) terms.add(cb.le(root.get("bookedPrice"), maxPrice));
            if (dateFrom != null) terms.add(
                cb.greaterThanOrEqualTo(root.get("timeSlot").get("startTime"), dateFrom)
            );
            if (dateTo != null) terms.add(cb.lessThanOrEqualTo(root.get("timeSlot").get("endTime"), dateTo));
            return cb.and(terms.toArray(Predicate[]::new));
        };
    }

    public Sort sort() {
        String field = switch (sortBy == null ? "time" : sortBy) {
            case "time" -> "timeSlot.startTime";
            case "status" -> "status";
            case "price" -> "bookedPrice";
            case "number" -> "bookingNumber";
            default -> throw new IllegalArgumentException("Неизвестное поле сортировки");
        };
        return Sort.by(
            direction == null ? Sort.Direction.ASC : Sort.Direction.fromString(direction),
            field
        ).and(Sort.by("id"));
    }
}
