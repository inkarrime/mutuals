package com.mutuals.subscription.dto;

import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(@NotNull BillingPeriod billingPeriod) {
}
