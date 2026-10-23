package ru.mirea.beautysalon.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.beautysalon.entity.BookingStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {
    private BookingStatus status;
}