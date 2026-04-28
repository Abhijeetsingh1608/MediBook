package com.medibook.review.service;
import com.medibook.review.client.AppointmentClient;
import com.medibook.review.client.ProviderClient;
import com.medibook.review.dto.AppointmentResponse;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for ReviewServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final String APPOINTMENT_COMPLETED = "COMPLETED";

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final ReviewRepository reviewRepository;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final AppointmentClient appointmentClient;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final ProviderClient providerClient;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public Review createReview(ReviewRequest request, Long patientUserId) {
        validateReviewRequest(request);

        if (reviewRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Review already exists for this appointment");
        }

        AppointmentResponse appointment = appointmentClient.getAppointmentById(request.getAppointmentId());
        validateAppointmentForReview(appointment, request, patientUserId);

        Review review = Review.builder()
                .appointmentId(request.getAppointmentId())
                .patientUserId(patientUserId)
                .providerId(request.getProviderId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        updateProviderAverageRating(request.getProviderId());
        return savedReview;
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Review> getReviewsByProvider(Long providerId) {
        return reviewRepository.findByProviderId(providerId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Review> getReviewsByPatient(Long patientUserId) {
        return reviewRepository.findByPatientUserId(patientUserId);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Review updateReview(Long reviewId, ReviewRequest request, Long patientUserId) {
        validateRating(request.getRating());

        Review review = getReviewById(reviewId);
        if (!review.getPatientUserId().equals(patientUserId)) {
            throw new RuntimeException("You are not allowed to update this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        updateProviderAverageRating(updatedReview.getProviderId());
        return updatedReview;
    }

    @Override
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public void deleteReview(Long reviewId, Long patientUserId, String role) {
        Review review = getReviewById(reviewId);

        if (!"ADMIN".equalsIgnoreCase(role) && !review.getPatientUserId().equals(patientUserId)) {
            throw new RuntimeException("You are not allowed to delete this review");
        }

        Long providerId = review.getProviderId();
        reviewRepository.delete(review);
        updateProviderAverageRating(providerId);
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateReviewRequest(ReviewRequest request) {
        if (request.getAppointmentId() == null) {
            throw new RuntimeException("Appointment id is required");
        }
        if (request.getProviderId() == null) {
            throw new RuntimeException("Provider id is required");
        }
        validateRating(request.getRating());
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateAppointmentForReview(
            AppointmentResponse appointment,
            ReviewRequest request,
            Long patientUserId) {

        if (appointment == null) {
            throw new RuntimeException("Appointment not found");
        }
        if (!patientUserId.equals(appointment.getPatientUserId())) {
            throw new RuntimeException("You can review only your own appointment");
        }
        if (!request.getProviderId().equals(appointment.getProviderId())) {
            throw new RuntimeException("Appointment does not belong to selected provider");
        }
        if (!APPOINTMENT_COMPLETED.equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Only completed appointments can be reviewed");
        }
    }

    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    private void updateProviderAverageRating(Long providerId) {
        Double averageRating = reviewRepository.calculateAverageRating(providerId);
        providerClient.updateProviderRating(providerId, averageRating == null ? 0.0 : averageRating);
    }
}
