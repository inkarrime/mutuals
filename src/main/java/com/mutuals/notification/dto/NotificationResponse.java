package com.mutuals.notification.dto;

import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationPriority priority,
        String title,
        String body,
        boolean read,
        String referenceType,
        Long referenceId,
        Instant createdAt
) {
}
