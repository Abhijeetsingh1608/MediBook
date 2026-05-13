package com.medibook.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationRecordTest {

    @Test
    void prePersist_setsCreatedAtAndDefaultChannel() {
        NotificationRecord record = NotificationRecord.builder()
                .userId(1L)
                .subject("Reminder")
                .build();

        record.prePersist();

        assertThat(record.getCreatedAt()).isNotNull();
        assertThat(record.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    }

    @Test
    void prePersist_keepsExplicitChannel() {
        NotificationRecord record = NotificationRecord.builder()
                .channel(NotificationChannel.APP)
                .build();

        record.prePersist();

        assertThat(record.getChannel()).isEqualTo(NotificationChannel.APP);
    }
}
