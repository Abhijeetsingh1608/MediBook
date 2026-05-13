package com.medibook.eureka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class EurekaServerApplicationTest {

    @Test
    void constructor_createsApplicationInstance() {
        assertThat(new EurekaServerApplication()).isNotNull();
    }

    @Test
    void main_startsApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            EurekaServerApplication.main(new String[] {"--spring.profiles.active=test"});

            springApplication.verify(() -> SpringApplication.run(EurekaServerApplication.class,
                    new String[] {"--spring.profiles.active=test"}));
        }
    }
}
