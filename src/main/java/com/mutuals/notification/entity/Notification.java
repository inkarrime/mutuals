package com.mutuals.notification.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient_read", columnList = "recipient_id, is_read"),
        @Index(name = "idx_notifications_recipient_created", columnList = "recipient_id, created_at")
})
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private NotificationPriority priority;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 300)
    @Column(length = 300)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    private Instant readAt;

    private Long referenceId;

    @Size(max = 30)
    @Column(length = 30)
    private String referenceType;
}
