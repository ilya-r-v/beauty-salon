package ru.mirea.beautysalon.dto.client;

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
public class MasterProfileResponse {
    private UUID clientId;
    private String name;
    private String avatarUrl;
    private String description;
    private Float rating;
}