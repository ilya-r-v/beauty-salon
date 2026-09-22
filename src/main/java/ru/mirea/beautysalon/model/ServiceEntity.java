package ru.mirea.beautysalon.model;

import java.util.UUID;

public class ServiceEntity {
    private UUID id;
    private String title;
    private UUID masterId;
    private UUID serviceTypeId;

    public ServiceEntity() {}

    public ServiceEntity(UUID id, String title, UUID masterId, UUID serviceTypeId) {
        this.id = id;
        this.title = title;
        this.masterId = masterId;
        this.serviceTypeId = serviceTypeId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public UUID getMasterId() { return masterId; }
    public void setMasterId(UUID masterId) { this.masterId = masterId; }

    public UUID getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(UUID serviceTypeId) { this.serviceTypeId = serviceTypeId; }
}
