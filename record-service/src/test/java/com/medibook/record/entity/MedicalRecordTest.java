package com.medibook.record.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MedicalRecordTest {

    @Test
    void lifecycleMethods_manageTimestamps() {
        MedicalRecord record = MedicalRecord.builder().appointmentId(1L).patientUserId(2L).providerId(3L).build();

        record.prePersist();
        LocalDateTime createdAt = record.getCreatedAt();
        LocalDateTime updatedAt = record.getUpdatedAt();

        record.preUpdate();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        assertThat(record.getUpdatedAt()).isNotNull();
        assertThat(record.getPrescriptions()).isNotNull();
    }
}
