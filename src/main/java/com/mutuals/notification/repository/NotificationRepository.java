package com.mutuals.notification.repository;

import com.mutuals.notification.entity.Notification;
import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndPriorityOrderByCreatedAtDesc(Long recipientId, NotificationPriority priority,
                                                                       Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    long countByRecipientIdAndPriorityAndReadFalse(Long recipientId, NotificationPriority priority);

    boolean existsByRecipientIdAndTypeAndReferenceIdAndCreatedAtAfter(Long recipientId, NotificationType type,
                                                                      Long referenceId, Instant after);

    @Modifying
    @Query("update Notification n set n.read = true, n.readAt = :now where n.recipient.id = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") Instant now);
}
