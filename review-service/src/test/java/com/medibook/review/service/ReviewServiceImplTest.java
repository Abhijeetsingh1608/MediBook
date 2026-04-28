package com.medibook.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.review.client.AppointmentClient;
import com.medibook.review.client.ProviderClient;
import com.medibook.review.dto.AppointmentResponse;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private ProviderClient providerClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewRequest reviewRequest;
    private AppointmentResponse completedAppointment;

    @BeforeEach
    void setUp() {
        reviewRequest = ReviewRequest.builder()
                .appointmentId(1L)
                .providerId(3L)
                .rating(5)
                .comment("Very helpful")
                .build();

        completedAppointment = new AppointmentResponse(
                1L,
                9L,
                3L,
                7L,
                "COMPLETED");
    }

    @Test
    @DisplayName("createReview: success - saves review and updates provider rating")
    void createReview_success() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(appointmentClient.getAppointmentById(1L)).thenReturn(completedAppointment);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setReviewId(1L);
            return review;
        });
        when(reviewRepository.calculateAverageRating(3L)).thenReturn(4.5);

        Review result = reviewService.createReview(reviewRequest, 9L);

        assertThat(result.getRating()).isEqualTo(5);
        verify(providerClient).updateProviderRating(3L, 4.5);
    }

    @Test
    @DisplayName("createReview: throws when review already exists for appointment")
    void createReview_duplicate_throwsException() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest, 9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updateReview: throws when patient is not owner")
    void updateReview_unauthorized_throwsException() {
        Review existing = Review.builder()
                .reviewId(1L)
                .appointmentId(1L)
                .patientUserId(9L)
                .providerId(3L)
                .rating(4)
                .comment("Old")
                .build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.updateReview(1L, reviewRequest, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("deleteReview: admin can delete and rating is recalculated")
    void deleteReview_admin_success() {
        Review existing = Review.builder()
                .reviewId(1L)
                .appointmentId(1L)
                .patientUserId(9L)
                .providerId(3L)
                .rating(4)
                .comment("Old")
                .build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reviewRepository.calculateAverageRating(3L)).thenReturn(null);

        reviewService.deleteReview(1L, 77L, "ADMIN");

        verify(reviewRepository).delete(existing);
        verify(providerClient).updateProviderRating(3L, 0.0);
    }
}
