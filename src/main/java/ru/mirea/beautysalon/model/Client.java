package ru.mirea.beautysalon.model;

import java.util.UUID;

public class Client {
    private UUID id;
    private String name;
    private String phone;
    private String passwordHash;
    private UUID roleId;

    public Client() {}

    public Client(UUID id, String name, String phone, String passwordHash, UUID roleId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    @Override
    public String toString() { return String.format("%s (%s)", name, phone); }
}
