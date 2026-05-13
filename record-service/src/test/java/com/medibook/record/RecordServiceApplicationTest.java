package com.medibook.record;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class RecordServiceApplicationTest {

    @Test
    void main_startsApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            RecordServiceApplication.main(new String[] {"--spring.profiles.active=test"});

            springApplication.verify(() -> SpringApplication.run(RecordServiceApplication.class,
                    new String[] {"--spring.profiles.active=test"}));
        }
    }
}
