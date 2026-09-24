package com.mutuals.streak.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateInteractionRequest(
        @NotNull @Positive Long friendId,
        @Size(max = 140) String note
) {
}
