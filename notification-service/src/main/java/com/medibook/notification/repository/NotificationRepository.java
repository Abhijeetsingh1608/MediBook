package com.medibook.notification.repository;

import com.medibook.notification.entity.NotificationRecord;
import com.medibook.notification.entity.NotificationChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for NotificationRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<NotificationRecord> findByUserIdAndReadStatusFalseOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndChannelAndSubject(Long userId, NotificationChannel channel, String subject);
}
