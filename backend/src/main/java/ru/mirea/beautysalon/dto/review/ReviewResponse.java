package ru.mirea.beautysalon.dto.review;

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
public class ReviewResponse {
    private UUID id;
    private UUID authorId;
    private String authorName;
    private UUID masterId;
    private String masterName;
    private String content;
}