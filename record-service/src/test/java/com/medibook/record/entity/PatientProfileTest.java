package com.medibook.record.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PatientProfileTest {

    @Test
    void lifecycleMethods_manageTimestamps() {
        PatientProfile profile = PatientProfile.builder().userId(5L).fullName("Patient").build();

        profile.prePersist();
        LocalDateTime createdAt = profile.getCreatedAt();
        LocalDateTime updatedAt = profile.getUpdatedAt();

        profile.preUpdate();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }
}
