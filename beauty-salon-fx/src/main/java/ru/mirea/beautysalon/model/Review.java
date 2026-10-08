package ru.mirea.beautysalon.model;

import java.util.UUID;

public class Review {
    private UUID id;
    private UUID authorId;
    private UUID masterId;
    private String content;

    public Review() {}

    public Review(UUID id, UUID authorId, UUID masterId, String content) {
        this.id = id;
        this.authorId = authorId;
        this.masterId = masterId;
        this.content = content;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public UUID getMasterId() { return masterId; }
    public void setMasterId(UUID masterId) { this.masterId = masterId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
