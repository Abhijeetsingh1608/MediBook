package com.medibook.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import com.medibook.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AdminBootstrapConfigTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AdminBootstrapConfig config;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        config = new AdminBootstrapConfig(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(config, "adminEmail", "admin@medibook.com");
        ReflectionTestUtils.setField(config, "adminPassword", "secret");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
    }

    @Test
    void createDefaultAdmin_createsAdminWhenMissing() throws Exception {
        when(userRepository.existsByEmail("admin@medibook.com")).thenReturn(false);

        ApplicationRunner runner = config.createDefaultAdmin();
        runner.run(Mockito.mock(ApplicationArguments.class));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createDefaultAdmin_upgradesExistingUserToAdmin() throws Exception {
        User existingUser = User.builder()
                .email("admin@medibook.com")
                .role(UserRole.PATIENT)
                .active(false)
                .emailVerified(false)
                .build();
        when(userRepository.existsByEmail("admin@medibook.com")).thenReturn(true);
        when(userRepository.findByEmail("admin@medibook.com")).thenReturn(java.util.Optional.of(existingUser));

        ApplicationRunner runner = config.createDefaultAdmin();
        runner.run(Mockito.mock(ApplicationArguments.class));

        assertThat(existingUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(existingUser.isActive()).isTrue();
        assertThat(existingUser.isEmailVerified()).isTrue();
        assertThat(existingUser.getPassword()).isEqualTo("encoded-secret");
        verify(userRepository).save(existingUser);
    }

    @Test
    void createDefaultAdmin_keepsAdminRoleForExistingAdmin() throws Exception {
        User existingUser = User.builder()
                .email("admin@medibook.com")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.existsByEmail("admin@medibook.com")).thenReturn(true);
        when(userRepository.findByEmail("admin@medibook.com")).thenReturn(java.util.Optional.of(existingUser));

        config.createDefaultAdmin().run(Mockito.mock(ApplicationArguments.class));

        assertThat(existingUser.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(existingUser);
    }
}
