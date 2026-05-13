package com.medibook.provider.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.provider.entity.Provider;
import com.medibook.provider.repository.ProviderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProviderServiceImplTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProviderEventPublisher providerEventPublisher;

    @InjectMocks
    private ProviderServiceImpl providerService;

    private Provider sampleProvider;

    @BeforeEach
    void setUp() {
        sampleProvider = Provider.builder()
                .providerId(1L)
                .userId(11L)
                .fullName("Dr. Siddharth")
                .specialization("Eye Specialist")
                .qualification("M.B.B.S")
                .experienceYears(3)
                .clinicName("People's Clinic")
                .clinicAddress("Indrapuri")
                .bio("Experienced eye specialist")
                .avgRating(BigDecimal.ZERO)
                .verified(false)
                .available(false)
                .build();
    }

    @Test
    @DisplayName("createProvider: success - saves provider as pending and publishes approval request")
    void createProvider_success() {
        when(providerRepository.findByUserId(11L)).thenReturn(Optional.empty());
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider provider = invocation.getArgument(0);
            provider.setProviderId(1L);
            return provider;
        });

        Provider result = providerService.createProvider(sampleProvider);

        assertThat(result.isVerified()).isFalse();
        assertThat(result.isAvailable()).isFalse();
        verify(providerEventPublisher).publishProviderApprovalRequested(any());
    }

    @Test
    @DisplayName("createProvider: throws when same user already has provider profile")
    void createProvider_duplicateUser_throwsException() {
        when(providerRepository.findByUserId(11L)).thenReturn(Optional.of(sampleProvider));

        assertThatThrownBy(() -> providerService.createProvider(sampleProvider))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(providerRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProvidersBySpecialization: returns only verified and available providers")
    void getProvidersBySpecialization_filtersVerifiedAndAvailable() {
        Provider verifiedAvailable = Provider.builder()
                .providerId(1L).userId(11L).fullName("A").specialization("Eye").qualification("MBBS")
                .verified(true).available(true).avgRating(BigDecimal.ZERO).build();
        Provider verifiedUnavailable = Provider.builder()
                .providerId(2L).userId(12L).fullName("B").specialization("Eye").qualification("MBBS")
                .verified(true).available(false).avgRating(BigDecimal.ZERO).build();

        when(providerRepository.findBySpecializationIgnoreCase("Eye"))
                .thenReturn(List.of(verifiedAvailable, verifiedUnavailable));

        List<Provider> result = providerService.getProvidersBySpecialization("Eye");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProviderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateVerificationStatus: marks provider verified, available, and publishes approval once")
    void updateVerificationStatus_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Provider result = providerService.updateVerificationStatus(1L, true);

        assertThat(result.isVerified()).isTrue();
        assertThat(result.isAvailable()).isTrue();
        verify(providerEventPublisher).publishProviderApproved(any());
    }

    @Test
    @DisplayName("updateRating: updates average rating")
    void updateRating_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Provider result = providerService.updateRating(1L, BigDecimal.valueOf(4.5));

        assertThat(result.getAvgRating()).isEqualByComparingTo("4.5");
    }

    @Test
    void getAllProviders_success() {
        when(providerRepository.findAll()).thenReturn(List.of(sampleProvider));
        List<Provider> result = providerService.getAllProviders();
        assertThat(result).hasSize(1);
    }

    @Test
    void getProviderById_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        Provider result = providerService.getProviderById(1L);
        assertThat(result.getProviderId()).isEqualTo(1L);
    }

    @Test
    void getProviderById_notFound_throwsException() {
        when(providerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> providerService.getProviderById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getProviderByUserId_success() {
        when(providerRepository.findByUserId(11L)).thenReturn(Optional.of(sampleProvider));
        Provider result = providerService.getProviderByUserId(11L);
        assertThat(result.getUserId()).isEqualTo(11L);
    }

    @Test
    void searchProviders_success() {
        sampleProvider.setVerified(true);
        sampleProvider.setAvailable(true);
        when(providerRepository.searchProviders("test")).thenReturn(List.of(sampleProvider));
        List<Provider> result = providerService.searchProviders("test");
        assertThat(result).hasSize(1);
    }

    @Test
    void getProvidersByVerificationStatus_success() {
        when(providerRepository.findByVerified(true)).thenReturn(List.of(sampleProvider));
        List<Provider> result = providerService.getProvidersByVerificationStatus(true);
        assertThat(result).hasSize(1);
    }

    @Test
    void getProvidersByAvailability_success() {
        when(providerRepository.findByAvailable(true)).thenReturn(List.of(sampleProvider));
        List<Provider> result = providerService.getProvidersByAvailability(true);
        assertThat(result).hasSize(1);
    }

    @Test
    void countBySpecialization_success() {
        when(providerRepository.countBySpecializationIgnoreCase("Eye")).thenReturn(5L);
        long result = providerService.countBySpecialization("Eye");
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void updateProvider_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(i -> i.getArgument(0));

        Provider update = Provider.builder().fullName("Updated Name").build();
        Provider result = providerService.updateProvider(1L, update);

        assertThat(result.getFullName()).isEqualTo("Updated Name");
    }

    @Test
    void updateAvailability_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(i -> i.getArgument(0));

        Provider result = providerService.updateAvailability(1L, true);
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void deleteProvider_success() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        providerService.deleteProvider(1L);
        verify(providerRepository).delete(sampleProvider);
    }

    @Test
    void updateVerificationStatus_alreadyVerified_doesNotPublishEvent() {
        sampleProvider.setVerified(true);
        when(providerRepository.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        providerService.updateVerificationStatus(1L, true);

        verify(providerEventPublisher, never()).publishProviderApproved(any());
    }

    @Test
    void getProvidersBySpecialization_filtersOutUnverified() {
        Provider unverified = Provider.builder().verified(false).available(true).build();
        when(providerRepository.findBySpecializationIgnoreCase("Eye")).thenReturn(List.of(unverified));

        List<Provider> result = providerService.getProvidersBySpecialization("Eye");
        assertThat(result).isEmpty();
    }
}
