package com.medibook.review.repository;

import com.medibook.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*
 * This is the service interface for ReviewRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProviderId(Long providerId);

    List<Review> findByPatientUserId(Long patientUserId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("select avg(r.rating) from Review r where r.providerId = :providerId")
    Double calculateAverageRating(Long providerId);
}
