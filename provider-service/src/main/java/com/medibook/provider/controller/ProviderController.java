package com.medibook.provider.controller;

import com.medibook.provider.dto.ProviderRequest;
import com.medibook.provider.entity.Provider;
import com.medibook.provider.service.ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
/*
 * This controller handles API requests for ProviderController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class ProviderController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final ProviderService providerService;

    @PostMapping
    @Operation(summary = "Create provider profile for logged-in provider")
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public Provider createProvider(@RequestBody ProviderRequest request, HttpServletRequest httpRequest) {
        Long userId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");

        if (!"PROVIDER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only provider users can create provider profile");
        }

        Provider provider = Provider.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .clinicName(request.getClinicName())
                .clinicAddress(request.getClinicAddress())
                .verified(false)
                .available(true)
                .build();

        return providerService.createProvider(provider);
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Provider> getAllProviders(HttpServletRequest httpRequest) {
        String role = httpRequest.getHeader("X-User-Role");
        if ("ADMIN".equalsIgnoreCase(role)) {
            return providerService.getAllProviders();
        }
        return providerService.getProvidersByVerificationStatus(true).stream()
                .filter(Provider::isAvailable)
                .toList();
    }

    @GetMapping("/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Provider getProviderById(@PathVariable Long providerId, HttpServletRequest httpRequest) {
        Provider provider = providerService.getProviderById(providerId);
        String role = httpRequest.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role) && (!provider.isVerified() || !provider.isAvailable())) {
            throw new RuntimeException("Provider is not available for patients yet");
        }
        return provider;
    }

    @GetMapping("/user/{userId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Provider getProviderByUserId(@PathVariable Long userId) {
        return providerService.getProviderByUserId(userId);
    }

    @GetMapping("/specialization/{specialization}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersBySpecialization(@PathVariable String specialization) {
        return providerService.getProvidersBySpecialization(specialization);
    }

    @GetMapping("/search")
    /*
     * This method is used for search functionality.
     * It helps user find matching data using keyword or text input.
     */
    public List<Provider> searchProviders(@RequestParam String keyword) {
        return providerService.searchProviders(keyword);
    }

    @GetMapping("/verified")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersByVerificationStatus(@RequestParam boolean value) {
        return providerService.getProvidersByVerificationStatus(value);
    }

    @GetMapping("/available")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Provider> getProvidersByAvailability(@RequestParam boolean value) {
        return providerService.getProvidersByAvailability(value);
    }

    @GetMapping("/count")
    /*
     * This method returns count data instead of full records.
     * It is useful for summary cards, dashboards, or quick statistics.
     */
    public long countBySpecialization(@RequestParam String specialization) {
        return providerService.countBySpecialization(specialization);
    }

    @PutMapping("/{providerId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateProvider(@PathVariable Long providerId, @RequestBody Provider provider) {
        return providerService.updateProvider(providerId, provider);
    }

    @PutMapping("/{providerId}/verify")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateVerificationStatus(
            @PathVariable Long providerId,
            @RequestParam boolean verified,
            HttpServletRequest httpRequest) {
        String role = httpRequest.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only admin can approve provider profiles");
        }
        return providerService.updateVerificationStatus(providerId, verified);
    }

    @PutMapping("/{providerId}/availability")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateAvailability(@PathVariable Long providerId, @RequestParam boolean available) {
        return providerService.updateAvailability(providerId, available);
    }

    @PutMapping("/{providerId}/rating")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public Provider updateRating(@PathVariable Long providerId, @RequestParam BigDecimal avgRating) {
        return providerService.updateRating(providerId, avgRating);
    }

    @DeleteMapping("/{providerId}")
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public String deleteProvider(@PathVariable Long providerId) {
        providerService.deleteProvider(providerId);
        return "Provider deleted successfully";
    }
}
