package com.medibook.auth.dto;

import com.medibook.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for RegisterRequest.
 * It helps transfer only the required fields between layers.
 */
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private UserRole role;
}
