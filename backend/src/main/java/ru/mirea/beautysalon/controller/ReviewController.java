package ru.mirea.beautysalon.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.review.ReviewRequest;
import ru.mirea.beautysalon.dto.review.ReviewResponse;
import ru.mirea.beautysalon.exception.ForbiddenException;
import ru.mirea.beautysalon.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewResponse> getReviews(@RequestParam(required = false) UUID masterId) {
        return reviewService.getReviews(masterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID fallbackUserId) {
        UUID currentUserId = extractUserId(authentication, fallbackUserId);
        return reviewService.createReview(currentUserId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID fallbackUserId) {
        UUID currentUserId = extractUserId(authentication, fallbackUserId);
        reviewService.deleteReview(id, currentUserId);
    }

    private UUID extractUserId(Authentication authentication, UUID fallbackUserId) {
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            return userId;
        }
        if (authentication != null && authentication.getName() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (fallbackUserId != null) {
            return fallbackUserId;
        }
        throw new ForbiddenException("Пользователь не авторизован");
    }
}