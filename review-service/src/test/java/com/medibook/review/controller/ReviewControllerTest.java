package com.medibook.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.review.dto.ReviewRequest;
import com.medibook.review.entity.Review;
import com.medibook.review.service.ReviewService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new com.medibook.review.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReview_success() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        Review review = Review.builder().reviewId(1L).build();

        when(reviewService.createReview(any(), eq(10L))).thenReturn(review);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1));
    }

    @Test
    void getAllReviews_success() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(Review.builder().reviewId(1L).build()));

        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1));
    }

    @Test
    void getReviewsByProvider_success() throws Exception {
        when(reviewService.getReviewsByProvider(1L)).thenReturn(List.of(Review.builder().reviewId(1L).build()));

        mockMvc.perform(get("/api/v1/reviews/provider/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1));
    }

    @Test
    void createReview_notPatient_throwsException() throws Exception {
        ReviewRequest request = new ReviewRequest();

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PROVIDER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReview_success() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/1")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));
    }
}
