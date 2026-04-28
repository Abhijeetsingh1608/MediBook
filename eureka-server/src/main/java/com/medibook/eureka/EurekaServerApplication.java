package com.medibook.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
/*
 * This is the main starting class for this Spring Boot service.
 * When we run the application, execution begins from here.
 */
public class EurekaServerApplication {

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
