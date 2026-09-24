package ru.mirea.beautysalon.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    private UUID id;
    private UUID serviceVariantId;
    private BookingStatus status;
    private UUID masterId;
    private LocalDateTime time;
    private UUID clientId;

    public Booking() {}

    public Booking(UUID id, UUID serviceVariantId, BookingStatus status,
                   UUID masterId, LocalDateTime time, UUID clientId) {
        this.id = id;
        this.serviceVariantId = serviceVariantId;
        this.status = status;
        this.masterId = masterId;
        this.time = time;
        this.clientId = clientId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getServiceVariantId() { return serviceVariantId; }
    public void setServiceVariantId(UUID serviceVariantId) { this.serviceVariantId = serviceVariantId; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public UUID getMasterId() { return masterId; }
    public void setMasterId(UUID masterId) { this.masterId = masterId; }

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }

    @Override
    public String toString() {
        return String.format("Booking[id=%s, status=%s, time=%s]", id, status, time);
    }
}
