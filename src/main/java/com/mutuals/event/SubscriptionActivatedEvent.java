package com.mutuals.event;

import java.time.Instant;

public record SubscriptionActivatedEvent(Long subscriptionId, Long userId, Instant expiresAt) {
}
