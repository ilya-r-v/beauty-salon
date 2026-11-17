package ru.mirea.beautysalon.dto.booking;

import jakarta.validation.constraints.*;
import java.util.Set;

public record BookingEdit(
    @NotNull @PositiveOrZero Long version,
    @Size(max = 500) String comment,
    boolean prepaid,
    @NotNull Set<@NotBlank @Size(max = 50) String> tags
) {}
