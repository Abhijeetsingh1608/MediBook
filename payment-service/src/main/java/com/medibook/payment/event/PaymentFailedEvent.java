package com.medibook.payment.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private Long paymentId;
    private Long appointmentId;
    private Long patientUserId;
    private Long providerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String failureReason;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
