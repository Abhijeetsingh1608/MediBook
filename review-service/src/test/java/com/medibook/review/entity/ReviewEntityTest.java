package com.medibook.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReviewEntityTest {

    @Test
    void testReviewEntity() {
        Review review = Review.builder()
                .reviewId(1L)
                .rating(5)
                .comment("Good")
                .build();
        assertThat(review.getReviewId()).isEqualTo(1L);
        assertThat(review.getRating()).isEqualTo(5);
        
        review.setRating(4);
        assertThat(review.getRating()).isEqualTo(4);
    }

    @Test
    void lifecycleMethods_manageTimestamps() {
        Review review = Review.builder()
                .appointmentId(10L)
                .patientUserId(20L)
                .providerId(30L)
                .build();

        review.prePersist();
        LocalDateTime createdAt = review.getCreatedAt();
        LocalDateTime updatedAt = review.getUpdatedAt();
        review.preUpdate();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        assertThat(review.getUpdatedAt()).isNotNull();
    }
}
