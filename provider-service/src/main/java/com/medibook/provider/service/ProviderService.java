package com.medibook.provider.service;

import com.medibook.provider.entity.Provider;
import java.math.BigDecimal;
import java.util.List;

/*
 * This is the service interface for ProviderService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface ProviderService {

    /*
     * This method is used to create and save new data.
     * It takes input and returns the final saved object.
     */
    Provider createProvider(Provider provider);

    /*
     * This method fetches data using a specific id or filter value.
     * It returns one record or filtered list based on input.
     */
    Provider getProviderById(Long providerId);

    /*
     * This method fetches data using a specific id or filter value.
     * It returns one record or filtered list based on input.
     */
    Provider getProviderByUserId(Long userId);

    /*
     * This method fetches all records for this module.
     * It is useful when complete list data is needed on screen.
     */
    List<Provider> getAllProviders();

    /*
     * This method fetches data using a specific id or filter value.
     * It returns one record or filtered list based on input.
     */
    List<Provider> getProvidersBySpecialization(String specialization);

    /*
     * This method is used for search functionality.
     * It helps find matching data using a keyword or text input.
     */
    List<Provider> searchProviders(String keyword);

    /*
     * This method fetches data using a specific id or filter value.
     * It returns one record or filtered list based on input.
     */
    List<Provider> getProvidersByVerificationStatus(boolean verified);

    /*
     * This method fetches data using a specific id or filter value.
     * It returns one record or filtered list based on input.
     */
    List<Provider> getProvidersByAvailability(boolean available);

    /*
     * This method updates existing data with new values.
     * It is used when stored details or status need to change.
     */
    Provider updateProvider(Long providerId, Provider provider);

    /*
     * This method updates existing data with new values.
     * It is used when stored details or status need to change.
     */
    Provider updateVerificationStatus(Long providerId, boolean verified);

    /*
     * This method updates existing data with new values.
     * It is used when stored details or status need to change.
     */
    Provider updateAvailability(Long providerId, boolean available);

    /*
     * This method updates existing data with new values.
     * It is used when stored details or status need to change.
     */
    Provider updateRating(Long providerId, BigDecimal avgRating);

    /*
     * This method deletes the selected record from the system.
     * It is mostly used by admin or owner when data should be removed.
     */
    void deleteProvider(Long providerId);

    /*
     * This method returns count data instead of full records.
     * It is useful for summary cards or dashboard statistics.
     */
    long countBySpecialization(String specialization);

    /*
     * Rejects a provider with an optional admin note.
     * Sets verified=false and stores the rejection reason.
     */
    Provider rejectProvider(Long providerId, String note);
}
