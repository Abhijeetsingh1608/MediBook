package com.medibook.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
/*
 * This class is an important part of the PasswordConfig flow.
 * It supports the working of this module in the project.
 */
public class PasswordConfig {

    @Bean
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
