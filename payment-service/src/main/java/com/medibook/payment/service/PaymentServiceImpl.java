package com.medibook.payment.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import com.medibook.payment.client.AppointmentClient;
import com.medibook.payment.client.RazorpayClient;
import com.medibook.payment.dto.AppointmentResponse;
import com.medibook.payment.dto.CreatePaymentOrderRequest;
import com.medibook.payment.dto.PaymentOrderResponse;
import com.medibook.payment.dto.VerifyPaymentRequest;
import com.medibook.payment.event.PaymentFailedEvent;
import com.medibook.payment.entity.Payment;
import com.medibook.payment.entity.PaymentPurpose;
import com.medibook.payment.entity.PaymentStatus;
import com.medibook.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for PaymentServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY_INR = "INR";
    private static final String APPOINTMENT_CANCELLED = "CANCELLED";

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final PaymentRepository paymentRepository;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final AppointmentClient appointmentClient;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final RazorpayClient razorpayClient;
    /*
     * This publisher sends payment related events to RabbitMQ
     * so notification-service can email the patient automatically.
     */
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public PaymentOrderResponse createPaymentOrder(
            CreatePaymentOrderRequest request,
            Long loggedInUserId,
            String role) {

        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only patient users can create payment orders");
        }

        AppointmentResponse appointment = appointmentClient.getAppointmentById(request.getAppointmentId());
        if (appointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        if (!appointment.getPatientUserId().equals(loggedInUserId)) {
            throw new RuntimeException("You can pay only for your own appointment");
        }

        if (APPOINTMENT_CANCELLED.equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Cancelled appointment cannot be paid");
        }

        if (paymentRepository.existsByAppointmentIdAndStatus(request.getAppointmentId(), PaymentStatus.PAID)) {
            throw new RuntimeException("Payment is already completed for this appointment");
        }

        BigDecimal normalizedAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        Long amountPaisa = normalizedAmount.multiply(BigDecimal.valueOf(100)).longValueExact();
        String receipt = "appt_" + request.getAppointmentId() + "_" + System.currentTimeMillis();

        try {
            Map razorpayOrder = razorpayClient.createOrder(amountPaisa, CURRENCY_INR, receipt);

            if (razorpayOrder == null || razorpayOrder.get("id") == null) {
                throw new RuntimeException("Unable to create Razorpay order");
            }

            Payment payment = Payment.builder()
                    .appointmentId(appointment.getAppointmentId())
                    .patientUserId(appointment.getPatientUserId())
                    .providerId(appointment.getProviderId())
                    .amount(normalizedAmount)
                    .amountPaisa(amountPaisa)
                    .currency(CURRENCY_INR)
                    .purpose(PaymentPurpose.APPOINTMENT_CONSULTATION)
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(String.valueOf(razorpayOrder.get("id")))
                    .receipt(receipt)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            return PaymentOrderResponse.builder()
                    .razorpayKeyId(razorpayClient.getKeyId())
                    .razorpayOrderId(savedPayment.getRazorpayOrderId())
                    .amountPaisa(savedPayment.getAmountPaisa())
                    .currency(savedPayment.getCurrency())
                    .payment(savedPayment)
                    .build();
        } catch (RuntimeException ex) {
            releaseFailedAppointmentBooking(request.getAppointmentId());
            throw ex;
        }
    }

    @Override
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public Payment verifyPayment(VerifyPaymentRequest request, Long loggedInUserId, String role) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment order not found"));

        validateOwnerOrAdmin(payment, loggedInUserId, role);

        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return payment;
        }

        if (!isValidRazorpaySignature(request)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid Razorpay signature");
            Payment failedPayment = paymentRepository.save(payment);
            publishPaymentFailedEvent(failedPayment);
            return failedPayment;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentMethod("RAZORPAY");
        payment.setPaidAt(LocalDateTime.now());
        payment.setFailureReason(null);
        Payment savedPayment = paymentRepository.save(payment);
        activatePaidAppointment(savedPayment.getAppointmentId());
        return savedPayment;
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public Payment markPaymentFailed(Long paymentId, Long loggedInUserId, String role) {
        Payment payment = findById(paymentId);
        validateOwnerOrAdmin(payment, loggedInUserId, role);

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return payment;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Payment failed or cancelled by user");
        Payment failedPayment = paymentRepository.save(payment);
        publishPaymentFailedEvent(failedPayment);
        return failedPayment;
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public Payment refundPayment(Long paymentId, String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only admin can refund payment");
        }

        Payment payment = findById(paymentId);
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only paid payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Payment getPaymentById(Long paymentId, Long loggedInUserId, String role) {
        Payment payment = findById(paymentId);
        validateOwnerOrAdmin(payment, loggedInUserId, role);
        return payment;
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Payment> getAllPayments(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only admin can view all payments");
        }
        return paymentRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Payment> getPaymentsByPatient(Long patientUserId, Long loggedInUserId, String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !patientUserId.equals(loggedInUserId)) {
            throw new RuntimeException("You can view only your own payments");
        }
        return paymentRepository.findByPatientUserIdOrderByCreatedAtDesc(patientUserId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Payment> getPaymentsByProvider(Long providerId) {
        return paymentRepository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    private Payment findById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateOwnerOrAdmin(Payment payment, Long loggedInUserId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        if (loggedInUserId == null || !payment.getPatientUserId().equals(loggedInUserId)) {
            throw new RuntimeException("You can access only your own payments");
        }
    }

    /*
     * This helper method returns true or false based on a condition.
     * It keeps validation logic reusable and clean.
     */
    private boolean isValidRazorpaySignature(VerifyPaymentRequest request) {
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String generatedSignature = hmacSha256(payload, razorpayClient.getKeySecret());
        return generatedSignature.equals(request.getRazorpaySignature());
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException("Unable to verify Razorpay signature");
        }
    }

    /*
     * This helper releases the appointment if payment setup fails before checkout starts.
     */
    private void releaseFailedAppointmentBooking(Long appointmentId) {
        try {
            appointmentClient.releaseFailedPaymentBooking(appointmentId);
        } catch (RuntimeException ex) {
            // If rollback also fails, we keep the original payment error for the caller.
        }
    }

    /*
     * This helper makes sure a paid appointment is active in the appointment module.
     */
    private void activatePaidAppointment(Long appointmentId) {
        try {
            appointmentClient.activatePaidAppointment(appointmentId);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Payment was successful, but appointment activation failed");
        }
    }

    /*
     * This helper creates one clean payment failure event
     * so notification-service can send mail to the patient.
     */
    private void publishPaymentFailedEvent(Payment payment) {
        AppointmentResponse appointment = null;
        try {
            appointment = appointmentClient.getAppointmentById(payment.getAppointmentId());
        } catch (RuntimeException ex) {
            appointment = null;
        }

        paymentEventPublisher.publishPaymentFailed(
                PaymentFailedEvent.builder()
                        .paymentId(payment.getPaymentId())
                        .appointmentId(payment.getAppointmentId())
                        .patientUserId(payment.getPatientUserId())
                        .providerId(payment.getProviderId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .paymentMethod(payment.getPaymentMethod())
                        .failureReason(payment.getFailureReason())
                        .appointmentDate(appointment == null ? null : appointment.getAppointmentDate())
                        .startTime(appointment == null ? null : appointment.getStartTime())
                        .endTime(appointment == null ? null : appointment.getEndTime())
                        .build());
    }

    @Override
    public byte[] generateInvoicePdf(Long paymentId, Long loggedInUserId, String role) {
        Payment payment = getPaymentById(paymentId, loggedInUserId, role);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Payment Invoice"));
            document.add(new Paragraph("----------------------------------"));
            document.add(new Paragraph("Payment ID: " + payment.getPaymentId()));
            document.add(new Paragraph("Appointment ID: " + payment.getAppointmentId()));
            document.add(new Paragraph("Patient ID: " + payment.getPatientUserId()));
            document.add(new Paragraph("Provider ID: " + payment.getProviderId()));
            document.add(new Paragraph("Amount: " + payment.getAmount() + " " + payment.getCurrency()));
            document.add(new Paragraph("Status: " + payment.getStatus()));
            if (payment.getRazorpayPaymentId() != null) {
                document.add(new Paragraph("Transaction ID: " + payment.getRazorpayPaymentId()));
            }
            if (payment.getPaidAt() != null) {
                document.add(new Paragraph("Paid At: " + payment.getPaidAt()));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }
}
