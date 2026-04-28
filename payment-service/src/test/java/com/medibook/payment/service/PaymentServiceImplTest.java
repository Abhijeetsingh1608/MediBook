package com.medibook.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.payment.client.AppointmentClient;
import com.medibook.payment.client.RazorpayClient;
import com.medibook.payment.dto.AppointmentResponse;
import com.medibook.payment.dto.CreatePaymentOrderRequest;
import com.medibook.payment.dto.PaymentOrderResponse;
import com.medibook.payment.dto.VerifyPaymentRequest;
import com.medibook.payment.entity.Payment;
import com.medibook.payment.entity.PaymentPurpose;
import com.medibook.payment.entity.PaymentStatus;
import com.medibook.payment.event.PaymentFailedEvent;
import com.medibook.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private AppointmentResponse activeAppointment;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        activeAppointment = new AppointmentResponse(
                1L,
                2L,
                3L,
                4L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                "General consultation",
                "BOOKED");

        pendingPayment = Payment.builder()
                .paymentId(100L)
                .appointmentId(1L)
                .patientUserId(2L)
                .providerId(3L)
                .amount(BigDecimal.valueOf(500.00))
                .amountPaisa(50000L)
                .currency("INR")
                .purpose(PaymentPurpose.APPOINTMENT_CONSULTATION)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_test_1")
                .receipt("appt_1_123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createPaymentOrder: throws when role is not patient")
    void createPaymentOrder_nonPatient_throwsException() {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, 2L, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only patient users");
    }

    @Test
    @DisplayName("createPaymentOrder: throws when appointment is cancelled")
    void createPaymentOrder_cancelledAppointment_throwsException() {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        activeAppointment.setStatus("CANCELLED");
        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, 2L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cancelled appointment");
    }

    @Test
    @DisplayName("createPaymentOrder: throws when paid payment already exists")
    void createPaymentOrder_duplicatePaidPayment_throwsException() {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);
        when(paymentRepository.existsByAppointmentIdAndStatus(1L, PaymentStatus.PAID)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, 2L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    @DisplayName("createPaymentOrder: success - creates pending payment and returns order response")
    void createPaymentOrder_success() {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);
        when(paymentRepository.existsByAppointmentIdAndStatus(1L, PaymentStatus.PAID)).thenReturn(false);
        when(razorpayClient.createOrder(
                eq(50000L),
                eq("INR"),
                org.mockito.ArgumentMatchers.startsWith("appt_1_")))
                .thenReturn(Map.of("id", "order_test_1"));
        when(razorpayClient.getKeyId()).thenReturn("rzp_test_key");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(100L);
            return payment;
        });

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, 2L, "PATIENT");

        assertThat(response.getRazorpayOrderId()).isEqualTo("order_test_1");
        assertThat(response.getAmountPaisa()).isEqualTo(50000L);
        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getPayment().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("createPaymentOrder: releases appointment when Razorpay order creation fails")
    void createPaymentOrder_razorpayFailure_releasesAppointment() {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);
        when(paymentRepository.existsByAppointmentIdAndStatus(1L, PaymentStatus.PAID)).thenReturn(false);
        when(razorpayClient.createOrder(
                eq(50000L),
                eq("INR"),
                org.mockito.ArgumentMatchers.startsWith("appt_1_")))
                .thenThrow(new RuntimeException("Razorpay unavailable"));

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, 2L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Razorpay unavailable");

        verify(appointmentClient).releaseFailedPaymentBooking(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("verifyPayment: marks payment paid and activates appointment for valid signature")
    void verifyPayment_success() {
        String keySecret = "razorpay-secret-key-123456789012345";
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_test_1")
                .razorpayPaymentId("pay_test_1")
                .razorpaySignature(buildSignature("order_test_1|pay_test_1", keySecret))
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_test_1")).thenReturn(java.util.Optional.of(pendingPayment));
        when(razorpayClient.getKeySecret()).thenReturn(keySecret);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.verifyPayment(request, 2L, "PATIENT");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getPaymentMethod()).isEqualTo("RAZORPAY");
        assertThat(result.getRazorpayPaymentId()).isEqualTo("pay_test_1");
        verify(appointmentClient).activatePaidAppointment(1L);
    }

    @Test
    @DisplayName("verifyPayment: marks payment failed and publishes event for invalid signature")
    void verifyPayment_invalidSignature_marksFailed() {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_test_1")
                .razorpayPaymentId("pay_test_1")
                .razorpaySignature("wrong-signature")
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_test_1")).thenReturn(java.util.Optional.of(pendingPayment));
        when(razorpayClient.getKeySecret()).thenReturn("razorpay-secret-key-123456789012345");
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);

        Payment result = paymentService.verifyPayment(request, 2L, "PATIENT");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).contains("Invalid Razorpay signature");
        verify(paymentEventPublisher).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("markPaymentFailed: marks payment failed and publishes event")
    void markPaymentFailed_success() {
        when(paymentRepository.findById(100L)).thenReturn(java.util.Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentClient.getAppointmentById(1L)).thenReturn(activeAppointment);

        Payment result = paymentService.markPaymentFailed(100L, 2L, "PATIENT");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).contains("failed or cancelled");
        verify(paymentEventPublisher).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    @DisplayName("refundPayment: throws when non-admin tries to refund")
    void refundPayment_nonAdmin_throwsException() {
        assertThatThrownBy(() -> paymentService.refundPayment(100L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only admin");
    }

    @Test
    @DisplayName("getPaymentsByPatient: throws when patient tries to access another user's payments")
    void getPaymentsByPatient_nonOwner_throwsException() {
        assertThatThrownBy(() -> paymentService.getPaymentsByPatient(5L, 2L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only your own payments");
    }

    @Test
    @DisplayName("getAllPayments: returns all payments for admin")
    void getAllPayments_admin_success() {
        when(paymentRepository.findAll()).thenReturn(List.of(pendingPayment));

        List<Payment> result = paymentService.getAllPayments("ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPaymentById: returns payment to owner")
    void getPaymentById_owner_success() {
        when(paymentRepository.findById(100L)).thenReturn(java.util.Optional.of(pendingPayment));

        Payment result = paymentService.getPaymentById(100L, 2L, "PATIENT");

        assertThat(result.getPaymentId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getPaymentsByProvider: returns provider payment list")
    void getPaymentsByProvider_success() {
        when(paymentRepository.findByProviderIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(pendingPayment));

        List<Payment> result = paymentService.getPaymentsByProvider(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProviderId()).isEqualTo(3L);
    }

    private String buildSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Unable to build test signature", ex);
        }
    }
}
