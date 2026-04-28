package com.medibook.review.controller;

import com.medibook.review.dto.ApiMessage;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import com.medibook.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
/*
 * This controller handles API requests for ReviewController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class ReviewController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create review for a completed appointment")
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public Review createReview(@RequestBody ReviewRequest request, HttpServletRequest httpRequest) {
        Long patientUserId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");

        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only patient users can create reviews");
        }

        return reviewService.createReview(request, patientUserId);
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{reviewId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Review getReviewById(@PathVariable Long reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping("/provider/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Review> getReviewsByProvider(@PathVariable Long providerId) {
        return reviewService.getReviewsByProvider(providerId);
    }

    @GetMapping("/patient/{patientUserId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Review> getReviewsByPatient(@PathVariable Long patientUserId) {
        return reviewService.getReviewsByPatient(patientUserId);
    }

    @PutMapping("/{reviewId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Review updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request,
            HttpServletRequest httpRequest) {
        Long patientUserId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        return reviewService.updateReview(reviewId, request, patientUserId);
    }

    @DeleteMapping("/{reviewId}")
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public ApiMessage deleteReview(@PathVariable Long reviewId, HttpServletRequest httpRequest) {
        Long patientUserId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");
        reviewService.deleteReview(reviewId, patientUserId, role);
        return ApiMessage.builder()
                .message("Review deleted successfully")
                .build();
    }
}
