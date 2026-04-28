package com.medibook.auth.config;

import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import com.medibook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.email}")
    private String adminEmail;

    @Value("${app.admin.bootstrap.password}")
    private String adminPassword;

    @Bean
    public ApplicationRunner createDefaultAdmin() {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                User existingUser = userRepository.findByEmail(adminEmail)
                        .orElseThrow();
                if (existingUser.getRole() != UserRole.ADMIN) {
                    existingUser.setRole(UserRole.ADMIN);
                }
                existingUser.setEmailVerified(true);
                existingUser.setActive(true);
                existingUser.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(existingUser);
                return;
            }

            User adminUser = User.builder()
                    .fullName("MediBook Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .provider("LOCAL")
                    .active(true)
                    .emailVerified(true)
                    .build();

            userRepository.save(adminUser);
        };
    }
}


