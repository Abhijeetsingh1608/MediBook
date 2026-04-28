package com.medibook.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for ReviewRequest.
 * It helps transfer only the required fields between layers.
 */
public class ReviewRequest {
    private Long appointmentId;
    private Long providerId;
    private Integer rating;
    private String comment;
}
