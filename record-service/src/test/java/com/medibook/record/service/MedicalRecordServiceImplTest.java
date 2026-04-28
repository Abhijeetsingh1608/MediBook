package com.medibook.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.record.client.AppointmentClient;
import com.medibook.record.dto.AppointmentResponse;
import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.dto.PrescriptionItemRequest;
import com.medibook.record.entity.MedicalRecord;
import com.medibook.record.repository.MedicalRecordRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private MedicalRecordRequest request;
    private AppointmentResponse completedAppointment;

    @BeforeEach
    void setUp() {
        request = MedicalRecordRequest.builder()
                .appointmentId(1L)
                .symptoms("Fever")
                .diagnosis("Viral infection")
                .doctorNotes("Rest and hydration")
                .followUpDate(LocalDate.now().plusDays(7))
                .prescriptions(List.of(
                        PrescriptionItemRequest.builder().medicineName("Paracetamol").dosage("500mg").frequency("BD").build()))
                .build();

        completedAppointment = new AppointmentResponse(
                1L,
                9L,
                3L,
                7L,
                null,
                null,
                null,
                null,
                "COMPLETED");
    }

    @Test
    @DisplayName("createMedicalRecord: success - saves record with prescriptions")
    void createMedicalRecord_success() {
        when(medicalRecordRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(appointmentClient.getAppointmentById(1L)).thenReturn(completedAppointment);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> {
            MedicalRecord record = invocation.getArgument(0);
            record.setRecordId(1L);
            return record;
        });

        MedicalRecord result = medicalRecordService.createMedicalRecord(request, "PROVIDER");

        assertThat(result.getPatientUserId()).isEqualTo(9L);
        assertThat(result.getPrescriptions()).hasSize(1);
        assertThat(result.getPrescriptions().get(0).getMedicineName()).isEqualTo("Paracetamol");
    }

    @Test
    @DisplayName("createMedicalRecord: throws when appointment is not completed")
    void createMedicalRecord_nonCompleted_throwsException() {
        completedAppointment.setStatus("BOOKED");
        when(medicalRecordRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(appointmentClient.getAppointmentById(1L)).thenReturn(completedAppointment);

        assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(request, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only after appointment is completed");
    }

    @Test
    @DisplayName("getMedicalRecordById: patient cannot view another user's record")
    void getMedicalRecordById_unauthorized_throwsException() {
        MedicalRecord record = MedicalRecord.builder()
                .recordId(1L)
                .patientUserId(9L)
                .providerId(3L)
                .appointmentId(1L)
                .build();
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.getMedicalRecordById(1L, 99L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("own medical records");
    }
}
