package ru.mirea.beautysalon.model;

import java.util.UUID;

public class MasterProfile {
    private UUID clientId;
    private String avatarUrl;
    private String description;
    private float rating;

    public MasterProfile() {}

    public MasterProfile(UUID clientId, String avatarUrl, String description, float rating) {
        this.clientId = clientId;
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.rating = rating;
    }

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
