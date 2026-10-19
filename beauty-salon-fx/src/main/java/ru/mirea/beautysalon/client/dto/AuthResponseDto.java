package ru.mirea.beautysalon.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDto {

    private String token;
    private String tokenType;
    private UUID id;
    private String name;
    private String phone;
    private String role;

    public AuthResponseDto() {}

    public AuthResponseDto(String token, String tokenType, UUID id, String name, String phone, String role) {
        this.token = token;
        this.tokenType = tokenType;
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
