package com.mutuals.subscription.dto;

import com.mutuals.subscription.entity.SubscriptionPlan;
import com.mutuals.subscription.entity.SubscriptionStatus;

import java.time.Instant;
import java.util.List;

public record SubscriptionResponse(
        SubscriptionPlan plan,
        SubscriptionStatus status,
        Instant startedAt,
        Instant expiresAt,
        int maxActiveStreaks,
        List<String> benefits
) {
}
