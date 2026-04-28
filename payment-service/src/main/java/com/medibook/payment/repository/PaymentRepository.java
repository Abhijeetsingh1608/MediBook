package com.medibook.payment.repository;

import com.medibook.payment.entity.Payment;
import com.medibook.payment.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for PaymentRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByAppointmentIdAndStatus(Long appointmentId, PaymentStatus status);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByPatientUserIdOrderByCreatedAtDesc(Long patientUserId);

    List<Payment> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}
