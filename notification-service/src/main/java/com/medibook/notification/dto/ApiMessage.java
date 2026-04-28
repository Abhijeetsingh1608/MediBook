package com.medibook.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for ApiMessage.
 * It helps transfer only the required fields between layers.
 */
public class ApiMessage {
    private String message;
}
