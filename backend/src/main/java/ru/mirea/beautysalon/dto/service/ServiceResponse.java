package ru.mirea.beautysalon.dto.service;

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
public class ServiceResponse {
    private UUID id;
    private String title;
    private UUID masterId;
    private String masterName;
    private UUID serviceTypeId;
    private String serviceTypeName;
}