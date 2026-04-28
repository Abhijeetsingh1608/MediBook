package com.medibook.auth.repository;

import com.medibook.auth.entity.AuthOtp;
import com.medibook.auth.entity.AuthOtpPurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for AuthOtpRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface AuthOtpRepository extends JpaRepository<AuthOtp, Long> {

    Optional<AuthOtp> findTopByEmailAndPurposeOrderByIdDesc(String email, AuthOtpPurpose purpose);
}
