package ru.mirea.beautysalon.dto.review;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "ID мастера обязателен")
    private UUID masterId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;
}