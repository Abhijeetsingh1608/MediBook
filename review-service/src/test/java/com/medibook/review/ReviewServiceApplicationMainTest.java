package com.medibook.review;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ReviewServiceApplicationMainTest {

    @Test
    void main_startsApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ReviewServiceApplication.main(new String[] {"--spring.profiles.active=test"});

            springApplication.verify(() -> SpringApplication.run(ReviewServiceApplication.class,
                    new String[] {"--spring.profiles.active=test"}));
        }
    }
}
