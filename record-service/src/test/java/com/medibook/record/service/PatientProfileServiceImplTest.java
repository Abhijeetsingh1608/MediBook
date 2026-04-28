package com.medibook.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.medibook.record.dto.PatientProfileRequest;
import com.medibook.record.entity.PatientProfile;
import com.medibook.record.repository.PatientProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientProfileServiceImplTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @InjectMocks
    private PatientProfileServiceImpl patientProfileService;

    private PatientProfileRequest request;

    @BeforeEach
    void setUp() {
        request = PatientProfileRequest.builder()
                .fullName("Aditya")
                .phone("9999999999")
                .address("Bhopal")
                .build();
    }

    @Test
    @DisplayName("createProfile: success - creates patient profile for patient role")
    void createProfile_success() {
        when(patientProfileRepository.existsByUserId(9L)).thenReturn(false);
        when(patientProfileRepository.save(any(PatientProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientProfile result = patientProfileService.createProfile(request, 9L, "PATIENT");

        assertThat(result.getUserId()).isEqualTo(9L);
        assertThat(result.getFullName()).isEqualTo("Aditya");
    }

    @Test
    @DisplayName("createProfile: throws when profile already exists")
    void createProfile_duplicate_throwsException() {
        when(patientProfileRepository.existsByUserId(9L)).thenReturn(true);

        assertThatThrownBy(() -> patientProfileService.createProfile(request, 9L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updateProfile: throws when patient tries to update another profile")
    void updateProfile_unauthorized_throwsException() {
        assertThatThrownBy(() -> patientProfileService.updateProfile(9L, request, 99L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("own patient profile");
    }

    @Test
    @DisplayName("getProfileByUserId: returns profile when found")
    void getProfileByUserId_success() {
        PatientProfile profile = PatientProfile.builder().patientId(1L).userId(9L).fullName("Aditya").build();
        when(patientProfileRepository.findByUserId(9L)).thenReturn(Optional.of(profile));

        PatientProfile result = patientProfileService.getProfileByUserId(9L);

        assertThat(result.getFullName()).isEqualTo("Aditya");
    }
}
