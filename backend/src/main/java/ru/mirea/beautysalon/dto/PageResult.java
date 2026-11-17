package ru.mirea.beautysalon.dto;

import java.util.List;
import org.springframework.data.domain.*;

public record PageResult<T>(List<T> content, long totalElements, int number, int size) {
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    public static PageRequest request(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Размер страницы: 1–100");
        return PageRequest.of(page, size, sort);
    }
}
