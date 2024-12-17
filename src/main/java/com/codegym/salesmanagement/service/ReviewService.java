package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Review;
import com.codegym.salesmanagement.repository.IReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private IReviewRepository iReviewRepository;

    public void saveReview(Review review) {
        iReviewRepository.save(review);
    }

    public List<Review> getReviewsByProductId(Long productId) {
        return iReviewRepository.findByProductId(productId);
    }

    public void deleteReview(Long reviewId) {
        iReviewRepository.deleteById(reviewId);
    }

    public Review getReviewById(Long reviewId) {
        return iReviewRepository.findById(reviewId).orElse(null);
    }

    public double calculateAverageRating(Long productId) {
        List<Review> reviews = getReviewsByProductId(productId);
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
