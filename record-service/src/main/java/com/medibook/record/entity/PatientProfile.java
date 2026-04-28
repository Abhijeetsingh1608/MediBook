package com.medibook.record.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patient_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This entity represents the database table model for PatientProfile.
 * Each object of this class becomes one row in the related table.
 */
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String profilePicUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
