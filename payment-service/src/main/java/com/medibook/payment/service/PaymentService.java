package com.medibook.payment.service;

import com.medibook.payment.dto.CreatePaymentOrderRequest;
import com.medibook.payment.dto.PaymentOrderResponse;
import com.medibook.payment.dto.VerifyPaymentRequest;
import com.medibook.payment.entity.Payment;
import java.util.List;

/*
 * This is the service interface for PaymentService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface PaymentService {
    PaymentOrderResponse createPaymentOrder(CreatePaymentOrderRequest request, Long loggedInUserId, String role);

    Payment verifyPayment(VerifyPaymentRequest request, Long loggedInUserId, String role);

    Payment markPaymentFailed(Long paymentId, Long loggedInUserId, String role);

    Payment refundPayment(Long paymentId, String role);

    Payment getPaymentById(Long paymentId, Long loggedInUserId, String role);

    List<Payment> getAllPayments(String role);

    List<Payment> getPaymentsByPatient(Long patientUserId, Long loggedInUserId, String role);

    List<Payment> getPaymentsByProvider(Long providerId);
}
