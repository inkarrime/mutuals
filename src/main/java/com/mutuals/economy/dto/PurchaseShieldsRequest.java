package com.mutuals.economy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseShieldsRequest(@NotNull @Min(1) @Max(5) Integer quantity) {
}
