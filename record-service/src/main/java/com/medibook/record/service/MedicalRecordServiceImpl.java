package com.medibook.record.service;
import com.medibook.record.client.AppointmentClient;
import com.medibook.record.dto.AppointmentResponse;
import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.dto.PrescriptionItemRequest;
import com.medibook.record.entity.MedicalRecord;
import com.medibook.record.entity.PrescriptionItem;
import com.medibook.record.repository.MedicalRecordRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for MedicalRecordServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class MedicalRecordServiceImpl implements MedicalRecordService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final MedicalRecordRepository medicalRecordRepository;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final AppointmentClient appointmentClient;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public MedicalRecord createMedicalRecord(MedicalRecordRequest request, String role) {
        requireProviderOrAdmin(role);

        if (medicalRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Medical record already exists for this appointment");
        }

        AppointmentResponse appointment = appointmentClient.getAppointmentById(request.getAppointmentId());
        if (appointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Medical record can be created only after appointment is completed");
        }

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientUserId(appointment.getPatientUserId())
                .providerId(appointment.getProviderId())
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .doctorNotes(request.getDoctorNotes())
                .followUpDate(request.getFollowUpDate())
                .reportFileUrl(request.getReportFileUrl())
                .build();

        replacePrescriptions(medicalRecord, request.getPrescriptions());
        return medicalRecordRepository.save(medicalRecord);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public MedicalRecord getMedicalRecordById(Long recordId, Long loggedInUserId, String role) {
        MedicalRecord record = findById(recordId);
        if ("PATIENT".equalsIgnoreCase(role) && !record.getPatientUserId().equals(loggedInUserId)) {
            throw new RuntimeException("You can view only your own medical records");
        }
        return record;
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<MedicalRecord> getRecordsByPatient(Long patientUserId, Long loggedInUserId, String role) {
        if ("PATIENT".equalsIgnoreCase(role) && !patientUserId.equals(loggedInUserId)) {
            throw new RuntimeException("You can view only your own medical records");
        }
        return medicalRecordRepository.findByPatientUserId(patientUserId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<MedicalRecord> getRecordsByProvider(Long providerId) {
        return medicalRecordRepository.findByProviderId(providerId);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public MedicalRecord updateMedicalRecord(Long recordId, MedicalRecordRequest request, String role) {
        requireProviderOrAdmin(role);

        MedicalRecord medicalRecord = findById(recordId);
        medicalRecord.setSymptoms(request.getSymptoms());
        medicalRecord.setDiagnosis(request.getDiagnosis());
        medicalRecord.setDoctorNotes(request.getDoctorNotes());
        medicalRecord.setFollowUpDate(request.getFollowUpDate());
        medicalRecord.setReportFileUrl(request.getReportFileUrl());
        replacePrescriptions(medicalRecord, request.getPrescriptions());
        return medicalRecordRepository.save(medicalRecord);
    }

    @Override
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public void deleteMedicalRecord(Long recordId, String role) {
        requireProviderOrAdmin(role);
        medicalRecordRepository.delete(findById(recordId));
    }

    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    private MedicalRecord findById(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private void replacePrescriptions(MedicalRecord medicalRecord, List<PrescriptionItemRequest> requestItems) {
        medicalRecord.getPrescriptions().clear();

        if (requestItems == null) {
            return;
        }

        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemRequest requestItem : requestItems) {
            PrescriptionItem item = PrescriptionItem.builder()
                    .medicalRecord(medicalRecord)
                    .medicineName(requestItem.getMedicineName())
                    .dosage(requestItem.getDosage())
                    .frequency(requestItem.getFrequency())
                    .duration(requestItem.getDuration())
                    .instructions(requestItem.getInstructions())
                    .build();
            items.add(item);
        }

        medicalRecord.getPrescriptions().addAll(items);
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void requireProviderOrAdmin(String role) {
        if (!"PROVIDER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only provider or admin can manage medical records");
        }
    }
}
