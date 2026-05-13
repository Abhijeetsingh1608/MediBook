package com.medibook.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.review.client.AppointmentClient;
import com.medibook.review.client.ProviderClient;
import com.medibook.review.dto.AppointmentResponse;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import com.medibook.review.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    private ReviewRequest request;

    @BeforeEach
    void setUp() {
        request = new ReviewRequest();
        request.setAppointmentId(1L);
        request.setProviderId(2L);
        request.setRating(5);
        request.setComment("Great!");
    }

    @Test
    void createReview_success() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        AppointmentResponse appointment = new AppointmentResponse();
        appointment.setPatientUserId(10L);
        appointment.setProviderId(2L);
        appointment.setStatus("COMPLETED");
        when(appointmentClient.getAppointmentById(1L)).thenReturn(appointment);
        when(reviewRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(reviewRepository.calculateAverageRating(2L)).thenReturn(5.0);

        Review result = reviewService.createReview(request, 10L);
        assertThat(result.getRating()).isEqualTo(5);
        verify(providerClient).updateProviderRating(2L, 5.0);
    }

    @Test
    void createReview_alreadyExists_throwsException() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(true);
        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review already exists");
    }

    @Test
    void createReview_notCompleted_throwsException() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        AppointmentResponse appointment = new AppointmentResponse();
        appointment.setPatientUserId(10L);
        appointment.setProviderId(2L);
        appointment.setStatus("BOOKED");
        when(appointmentClient.getAppointmentById(1L)).thenReturn(appointment);

        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only completed appointments");
    }

    @Test
    void getReviewById_notFound_throwsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reviewService.getReviewById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void updateReview_unauthorized_throwsException() {
        Review review = Review.builder().patientUserId(20L).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertThatThrownBy(() -> reviewService.updateReview(1L, request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not allowed to update");
    }

    @Test
    void createReview_invalidRating_throwsException() {
        request.setRating(6);
        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void createReview_wrongPatient_throwsException() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        AppointmentResponse appointment = new AppointmentResponse();
        appointment.setPatientUserId(20L); // Not 10L
        when(appointmentClient.getAppointmentById(1L)).thenReturn(appointment);

        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only your own appointment");
    }

    @Test
    void createReview_wrongProvider_throwsException() {
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        AppointmentResponse appointment = new AppointmentResponse();
        appointment.setPatientUserId(10L);
        appointment.setProviderId(3L); // Not 2L
        when(appointmentClient.getAppointmentById(1L)).thenReturn(appointment);

        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not belong to selected provider");
    }

    @Test
    void deleteReview_admin_success() {
        Review review = Review.builder().reviewId(1L).patientUserId(20L).providerId(2L).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        
        reviewService.deleteReview(1L, 10L, "ADMIN");
        verify(reviewRepository).delete(review);
    }

    @Test
    void createReview_missingAppointmentId_throwsException() {
        request.setAppointmentId(null);
        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Appointment id is required");
    }

    @Test
    void createReview_missingProviderId_throwsException() {
        request.setProviderId(null);
        assertThatThrownBy(() -> reviewService.createReview(request, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Provider id is required");
    }
}
