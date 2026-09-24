package com.mutuals.streak.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInvitationRequest(@NotNull @Positive Long friendId) {
}
