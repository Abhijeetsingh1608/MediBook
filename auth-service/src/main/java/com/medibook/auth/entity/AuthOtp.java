package com.medibook.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This entity represents the database table model for AuthOtp.
 * Each object of this class becomes one row in the related table.
 */
public class AuthOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String otp;

    @Enumerated(EnumType.STRING)
    private AuthOtpPurpose purpose;

    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;
    private boolean used;

    @PrePersist
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
