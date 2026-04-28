package com.medibook.payment.controller;

import com.medibook.payment.dto.CreatePaymentOrderRequest;
import com.medibook.payment.dto.PaymentOrderResponse;
import com.medibook.payment.dto.VerifyPaymentRequest;
import com.medibook.payment.entity.Payment;
import com.medibook.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
/*
 * This controller handles API requests for PaymentController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class PaymentController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final PaymentService paymentService;

    @PostMapping("/orders")
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public PaymentOrderResponse createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            HttpServletRequest httpRequest) {
        return paymentService.createPaymentOrder(request, getUserId(httpRequest), getRole(httpRequest));
    }

    @PostMapping("/verify")
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public Payment verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            HttpServletRequest httpRequest) {
        return paymentService.verifyPayment(request, getUserId(httpRequest), getRole(httpRequest));
    }

    @PutMapping("/{paymentId}/failed")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public Payment markPaymentFailed(
            @PathVariable Long paymentId,
            HttpServletRequest httpRequest) {
        return paymentService.markPaymentFailed(paymentId, getUserId(httpRequest), getRole(httpRequest));
    }

    @PutMapping("/{paymentId}/refund")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public Payment refundPayment(
            @PathVariable Long paymentId,
            HttpServletRequest httpRequest) {
        return paymentService.refundPayment(paymentId, getRole(httpRequest));
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Payment> getAllPayments(HttpServletRequest httpRequest) {
        return paymentService.getAllPayments(getRole(httpRequest));
    }

    @GetMapping("/me")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public List<Payment> getMyPayments(HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return paymentService.getPaymentsByPatient(userId, userId, getRole(httpRequest));
    }

    @GetMapping("/{paymentId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Payment getPaymentById(
            @PathVariable Long paymentId,
            HttpServletRequest httpRequest) {
        return paymentService.getPaymentById(paymentId, getUserId(httpRequest), getRole(httpRequest));
    }

    @GetMapping("/patient/{patientUserId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Payment> getPaymentsByPatient(
            @PathVariable Long patientUserId,
            HttpServletRequest httpRequest) {
        return paymentService.getPaymentsByPatient(patientUserId, getUserId(httpRequest), getRole(httpRequest));
    }

    @GetMapping("/provider/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Payment> getPaymentsByProvider(@PathVariable Long providerId) {
        return paymentService.getPaymentsByProvider(providerId);
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        return userId == null ? null : Long.valueOf(userId);
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private String getRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }
}
