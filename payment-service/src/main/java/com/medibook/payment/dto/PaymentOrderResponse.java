package com.medibook.payment.dto;

import com.medibook.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for PaymentOrderResponse.
 * It helps transfer only the required fields between layers.
 */
public class PaymentOrderResponse {
    private String razorpayKeyId;
    private String razorpayOrderId;
    private Long amountPaisa;
    private String currency;
    private Payment payment;
}
