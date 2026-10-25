package ru.mirea.beautysalon.dto.master;

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
public class MasterResponse {
    private UUID clientId;
    private String name;
    private String phone;
    private String avatarUrl;
    private String description;
    private Double rating;
}