package com.medibook.review.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReviewDTOTest {

    @Test
    void testReviewRequest() {
        ReviewRequest request = new ReviewRequest();
        request.setAppointmentId(1L);
        request.setProviderId(2L);
        request.setRating(5);
        request.setComment("Great");
        assertThat(request.getAppointmentId()).isEqualTo(1L);
        assertThat(request.getRating()).isEqualTo(5);
    }

    @Test
    void testAppointmentResponse() {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(1L);
        response.setPatientUserId(10L);
        response.setProviderId(2L);
        response.setStatus("COMPLETED");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }
}
