package com.mutuals.streak.dto;

import jakarta.validation.constraints.NotNull;

public record RespondInvitationRequest(@NotNull Boolean accept) {
}
