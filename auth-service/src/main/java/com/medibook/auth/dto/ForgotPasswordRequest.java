package com.medibook.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for ForgotPasswordRequest.
 * It helps transfer only the required fields between layers.
 */
public class ForgotPasswordRequest {
    private String email;
}
