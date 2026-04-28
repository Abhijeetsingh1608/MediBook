package com.medibook.provider.service;

import com.medibook.provider.event.ProviderApprovalRequestedEvent;
import com.medibook.provider.event.ProviderApprovedEvent;
import com.medibook.provider.entity.Provider;
import com.medibook.provider.repository.ProviderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for ProviderServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class ProviderServiceImpl implements ProviderService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final ProviderRepository providerRepository;
    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final ProviderEventPublisher providerEventPublisher;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public Provider createProvider(Provider provider) {
        providerRepository.findByUserId(provider.getUserId()).ifPresent(existing -> {
            throw new RuntimeException("Provider profile already exists for this user");
        });

        provider.setVerified(false);
        provider.setAvailable(false);

        Provider savedProvider = providerRepository.save(provider);
        providerEventPublisher.publishProviderApprovalRequested(
                ProviderApprovalRequestedEvent.builder()
                        .providerId(savedProvider.getProviderId())
                        .userId(savedProvider.getUserId())
                        .fullName(savedProvider.getFullName())
                        .specialization(savedProvider.getSpecialization())
                        .qualification(savedProvider.getQualification())
                        .clinicName(savedProvider.getClinicName())
                        .clinicAddress(savedProvider.getClinicAddress())
                        .bio(savedProvider.getBio())
                        .build());
        return savedProvider;
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Provider getProviderById(Long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Provider getProviderByUserId(Long userId) {
        return providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for this user"));
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersBySpecialization(String specialization) {
        return providerRepository.findBySpecializationIgnoreCase(specialization).stream()
                .filter(Provider::isVerified)
                .filter(Provider::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    /*
     * This method is used for search functionality.
     * It helps user find matching data using keyword or text input.
     */
    public List<Provider> searchProviders(String keyword) {
        return providerRepository.searchProviders(keyword).stream()
                .filter(Provider::isVerified)
                .filter(Provider::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersByVerificationStatus(boolean verified) {
        return providerRepository.findByVerified(verified);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersByAvailability(boolean available) {
        return providerRepository.findByAvailable(available);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateProvider(Long providerId, Provider provider) {
        Provider existingProvider = getProviderById(providerId);
        existingProvider.setFullName(provider.getFullName());
        existingProvider.setSpecialization(provider.getSpecialization());
        existingProvider.setQualification(provider.getQualification());
        existingProvider.setExperienceYears(provider.getExperienceYears());
        existingProvider.setBio(provider.getBio());
        existingProvider.setClinicName(provider.getClinicName());
        existingProvider.setClinicAddress(provider.getClinicAddress());
        return providerRepository.save(existingProvider);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateVerificationStatus(Long providerId, boolean verified) {
        Provider provider = getProviderById(providerId);
        boolean wasVerified = provider.isVerified();
        provider.setVerified(verified);
        if (verified) {
            provider.setAvailable(true);
        }

        Provider updatedProvider = providerRepository.save(provider);
        if (!wasVerified && updatedProvider.isVerified()) {
            providerEventPublisher.publishProviderApproved(
                    ProviderApprovedEvent.builder()
                            .providerId(updatedProvider.getProviderId())
                            .userId(updatedProvider.getUserId())
                            .fullName(updatedProvider.getFullName())
                            .specialization(updatedProvider.getSpecialization())
                            .build());
        }
        return updatedProvider;
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateAvailability(Long providerId, boolean available) {
        Provider provider = getProviderById(providerId);
        provider.setAvailable(available);
        return providerRepository.save(provider);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateRating(Long providerId, BigDecimal avgRating) {
        Provider provider = getProviderById(providerId);
        provider.setAvgRating(avgRating);
        return providerRepository.save(provider);
    }

    @Override
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public void deleteProvider(Long providerId) {
        Provider provider = getProviderById(providerId);
        providerRepository.delete(provider);
    }

    @Override
    /*
     * This method returns count data instead of full records.
     * It is useful for summary cards, dashboards, or quick statistics.
     */
    public long countBySpecialization(String specialization) {
        return providerRepository.countBySpecializationIgnoreCase(specialization);
    }
}
