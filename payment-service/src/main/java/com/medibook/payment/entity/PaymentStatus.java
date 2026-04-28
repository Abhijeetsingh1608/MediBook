package com.medibook.payment.entity;

/*
 * This enum stores fixed constant values for PaymentStatus.
 * It helps avoid using random string values in the project.
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
