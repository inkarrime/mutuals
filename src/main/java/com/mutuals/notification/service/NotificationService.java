package com.mutuals.notification.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.notification.dto.NotificationResponse;
import com.mutuals.notification.dto.UnreadCountResponse;
import com.mutuals.notification.entity.Notification;
import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.entity.NotificationType;
import com.mutuals.notification.push.PushMessage;
import com.mutuals.notification.push.PushSender;
import com.mutuals.notification.repository.NotificationRepository;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.entity.Device;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.DeviceRepository;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PushSender pushSender;
    private final CurrentUserService currentUserService;
    private final Clock clock;

    @Transactional
    public void notify(Long recipientId, NotificationType type, NotificationPriority priority, String title,
                       String body, String referenceType, Long referenceId) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null || !recipient.isActive()) {
            return;
        }
        if (priority == NotificationPriority.LOW && recipient.getPreferences().isMuteHighlights()
                && type == NotificationType.HIGHLIGHT) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
        if (priority == NotificationPriority.HIGH && recipient.getPreferences().isPushEnabled()) {
            sendPush(recipientId, title, body, type, referenceType, referenceId);
        }
    }

    @Transactional(readOnly = true)
    public boolean wasNotifiedSince(Long recipientId, NotificationType type, Long referenceId, Instant since) {
        return notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndCreatedAtAfter(recipientId, type,
                referenceId, since);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(NotificationPriority priority, Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();
        return PageResponse.from(priority == null
                ? notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByRecipientIdAndPriorityOrderByCreatedAtDesc(userId, priority, pageable),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount() {
        Long userId = currentUserService.getCurrentUserId();
        return new UnreadCountResponse(
                notificationRepository.countByRecipientIdAndReadFalse(userId),
                notificationRepository.countByRecipientIdAndPriorityAndReadFalse(userId, NotificationPriority.HIGH),
                notificationRepository.countByRecipientIdAndPriorityAndReadFalse(userId, NotificationPriority.LOW));
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId,
                        currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(clock.instant());
        }
        return toResponse(notification);
    }

    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(currentUserService.getCurrentUserId(), clock.instant());
    }

    private void sendPush(Long recipientId, String title, String body, NotificationType type, String referenceType,
                          Long referenceId) {
        List<String> tokens = deviceRepository.findByUserId(recipientId).stream().map(Device::getFcmToken).toList();
        if (tokens.isEmpty()) {
            return;
        }
        Map<String, String> data = Map.of(
                "type", type.name(),
                "referenceType", referenceType == null ? "" : referenceType,
                "referenceId", referenceId == null ? "" : referenceId.toString());
        pushSender.send(tokens, new PushMessage(title, body == null ? "" : body, data));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getPriority(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getCreatedAt());
    }
}
