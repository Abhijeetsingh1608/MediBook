package com.medibook.provider.repository;

import com.medibook.provider.entity.Provider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * This is the service interface for ProviderRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(Long userId);

    List<Provider> findBySpecializationIgnoreCase(String specialization);

    List<Provider> findByVerified(boolean verified);

    List<Provider> findByAvailable(boolean available);

    List<Provider> findByClinicAddressContainingIgnoreCase(String clinicAddress);

    long countBySpecializationIgnoreCase(String specialization);

    @Query("""
            select p from Provider p
            where lower(p.fullName) like lower(concat('%', :keyword, '%'))
               or lower(p.specialization) like lower(concat('%', :keyword, '%'))
               or lower(p.clinicAddress) like lower(concat('%', :keyword, '%'))
            """)
    List<Provider> searchProviders(@Param("keyword") String keyword);
}
