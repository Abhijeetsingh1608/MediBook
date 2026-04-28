package com.medibook.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/*
 * This is the main starting class for this Spring Boot service.
 * When we run the application, execution begins from here.
 */
public class NotificationServiceApplication {

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
