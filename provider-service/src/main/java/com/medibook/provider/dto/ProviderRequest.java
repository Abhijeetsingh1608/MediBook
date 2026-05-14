package com.medibook.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for ProviderRequest.
 * It helps transfer only the required fields between layers.
 */
public class ProviderRequest {
    private String fullName;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String bio;
    private String clinicName;
    private String clinicAddress;
    private String documentUrl; // Medical license / experience letter URL
}
