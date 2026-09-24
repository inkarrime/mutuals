package com.mutuals.economy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GiftShieldRequest(@NotNull @Positive Long friendId) {
}
