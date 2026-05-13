package com.medibook.notification;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class NotificationServiceApplicationTest {

    @Test
    void main_startsApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            NotificationServiceApplication.main(new String[] {"--spring.profiles.active=test"});

            springApplication.verify(() -> SpringApplication.run(NotificationServiceApplication.class,
                    new String[] {"--spring.profiles.active=test"}));
        }
    }
}
