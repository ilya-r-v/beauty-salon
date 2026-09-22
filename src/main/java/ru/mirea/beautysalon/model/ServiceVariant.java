package ru.mirea.beautysalon.model;

import java.math.BigDecimal;
import java.util.UUID;

public class ServiceVariant {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private UUID serviceId;

    public ServiceVariant() {}

    public ServiceVariant(UUID id, String title, String description, BigDecimal price, UUID serviceId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.serviceId = serviceId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    @Override
    public String toString() { return String.format("%s — %s руб.", title, price); }
}
