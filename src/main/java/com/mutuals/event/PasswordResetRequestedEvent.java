package com.mutuals.event;


public record PasswordResetRequestedEvent(Long userId, String rawToken) {
}
