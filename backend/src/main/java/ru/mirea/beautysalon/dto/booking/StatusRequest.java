package ru.mirea.beautysalon.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.beautysalon.domain.BookingStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {
    @jakarta.validation.constraints.NotNull
    private Long version;
    private BookingStatus status;
}