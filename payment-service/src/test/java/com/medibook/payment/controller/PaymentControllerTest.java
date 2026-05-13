package com.medibook.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.payment.dto.CreatePaymentOrderRequest;
import com.medibook.payment.dto.PaymentOrderResponse;
import com.medibook.payment.dto.VerifyPaymentRequest;
import com.medibook.payment.entity.Payment;
import com.medibook.payment.entity.PaymentStatus;
import com.medibook.payment.service.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("POST /create-order - success")
    void createOrder_success() throws Exception {
        CreatePaymentOrderRequest request = CreatePaymentOrderRequest.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500))
                .build();

        PaymentOrderResponse response = PaymentOrderResponse.builder()
                .razorpayOrderId("order_1")
                .amountPaisa(50000L)
                .build();

        when(paymentService.createPaymentOrder(any(), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/orders")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razorpayOrderId").value("order_1"));
    }

    @Test
    @DisplayName("POST /verify - success")
    void verifyPayment_success() throws Exception {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .razorpaySignature("sig_1")
                .build();

        Payment payment = Payment.builder().status(PaymentStatus.PAID).build();
        when(paymentService.verifyPayment(any(), anyLong(), anyString())).thenReturn(payment);

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("GET / - admin access success")
    void getAllPayments_success() throws Exception {
        Payment payment = Payment.builder().paymentId(1L).build();
        when(paymentService.getAllPayments("ADMIN")).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/v1/payments")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(1));
    }

    @Test
    @DisplayName("GET /invoice/{paymentId} - success")
    void downloadInvoice_success() throws Exception {
        byte[] pdfContent = "dummy pdf".getBytes();
        when(paymentService.generateInvoicePdf(anyLong(), anyLong(), anyString())).thenReturn(pdfContent);

        mockMvc.perform(get("/api/v1/payments/1/invoice")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void markPaymentFailed_success() throws Exception {
        when(paymentService.markPaymentFailed(anyLong(), any(), any())).thenReturn(new Payment());

        mockMvc.perform(put("/api/v1/payments/1/failed")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk());
    }

    @Test
    void refundPayment_success() throws Exception {
        when(paymentService.refundPayment(anyLong(), any())).thenReturn(new Payment());

        mockMvc.perform(put("/api/v1/payments/1/refund")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyPayments_success() throws Exception {
        when(paymentService.getPaymentsByPatient(anyLong(), anyLong(), any())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/payments/me")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByPatient_success() throws Exception {
        when(paymentService.getPaymentsByPatient(anyLong(), anyLong(), anyString())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/payments/patient/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByProvider_success() throws Exception {
        when(paymentService.getPaymentsByProvider(anyLong())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/payments/provider/1"))
                .andExpect(status().isOk());
    }
}
