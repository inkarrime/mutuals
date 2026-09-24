package com.mutuals.notification.controller;

import com.mutuals.common.dto.MessageResponse;
import com.mutuals.common.dto.PageResponse;
import com.mutuals.notification.dto.NotificationResponse;
import com.mutuals.notification.dto.UnreadCountResponse;
import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "Notificaciones HIGH (push) y LOW (solo punto rojo)")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Mis notificaciones, opcionalmente filtradas por prioridad")
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @RequestParam(required = false) NotificationPriority priority,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(priority, pageable));
    }

    @Operation(summary = "Contador para el punto rojo")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        return ResponseEntity.ok(notificationService.unreadCount());
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markRead(notificationId));
    }

    @Operation(summary = "Marcar todas como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllRead() {
        return ResponseEntity.ok(new MessageResponse(notificationService.markAllRead() + " notifications marked as read"));
    }
}
