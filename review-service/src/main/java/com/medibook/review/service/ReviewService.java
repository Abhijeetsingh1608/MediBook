package com.medibook.review.service;

import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import java.util.List;

/*
 * This is the service interface for ReviewService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface ReviewService {

    Review createReview(ReviewRequest request, Long patientUserId);

    List<Review> getAllReviews();

    Review getReviewById(Long reviewId);

    List<Review> getReviewsByProvider(Long providerId);

    List<Review> getReviewsByPatient(Long patientUserId);

    Review updateReview(Long reviewId, ReviewRequest request, Long patientUserId);

    void deleteReview(Long reviewId, Long patientUserId, String role);
}
