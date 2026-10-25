package ru.mirea.beautysalon.dto.auth;

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
public class MeResponse {
    private UUID id;
    private String name;
    private String phone;
    private String role;
    private Boolean isMaster;
}