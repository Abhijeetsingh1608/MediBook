package com.medibook.auth.dto;

import com.medibook.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for AuthResponse.
 * It helps transfer only the required fields between layers.
 */
public class AuthResponse {
    private String token;
    private User user;
}
