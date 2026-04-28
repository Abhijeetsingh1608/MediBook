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
 * This DTO is used to carry request/response data for LoginRequest.
 * It helps transfer only the required fields between layers.
 */
public class LoginRequest {
    private String email;
    private String password;
    private String adminSecretKey;
}
