package ru.mirea.beautysalon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.mirea.beautysalon.dto.review.ReviewRequest;
import ru.mirea.beautysalon.dto.review.ReviewResponse;
import ru.mirea.beautysalon.entity.Client;
import ru.mirea.beautysalon.entity.Master;
import ru.mirea.beautysalon.entity.Review;
import ru.mirea.beautysalon.exception.BusinessException;
import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.exception.ForbiddenException;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.MasterRepository;
import ru.mirea.beautysalon.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ClientRepository clientRepository;
    private final MasterRepository masterRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(UUID masterId) {
        List<Review> reviews = (masterId != null)
                ? reviewRepository.findByMasterId(masterId)
                : reviewRepository.findAll();

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse createReview(UUID currentUserId, ReviewRequest request) {
        if (request.getContent() == null || request.getContent().trim().isBlank()) {
            throw new BusinessException("Текст отзыва не может быть пустым");
        }

        Client author = clientRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Автор отзыва не найден"));

        Master master = masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new EntityNotFoundException("Мастер с ID " + request.getMasterId() + " не найден"));

        Review review = Review.builder()
                .author(author)
                .master(master)
                .content(request.getContent().trim())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с ID " + reviewId + " не найден"));

        if (!review.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("Вы можете удалять только свои собственные отзывы");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .authorId(review.getAuthor().getId())
                .authorName(review.getAuthor().getName())
                .masterId(review.getMaster().getId())
                .masterName(review.getMaster().getClient().getName())
                .content(review.getContent())
                .build();
    }
}