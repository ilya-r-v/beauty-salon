package ru.mirea.beautysalon.dto.booking;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    @jakarta.validation.constraints.Size(max=80)
    private String externalCode;
    @jakarta.validation.constraints.Size(max=500)
    private String comment;
    private boolean prepaid;
    private java.util.Set<@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=50) String> tags;
    @jakarta.validation.constraints.NotNull
    private UUID clientId;
    @jakarta.validation.constraints.NotNull private UUID slotId;
    @jakarta.validation.constraints.NotNull private UUID serviceVariantId;
}