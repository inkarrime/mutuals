package com.mutuals.streak.dto;

import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.InteractionStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;
import java.time.LocalDate;

public record InteractionResponse(
        Long id,
        Long streakId,
        UserSummaryResponse initiator,
        UserSummaryResponse confirmer,
        InteractionMethod method,
        InteractionStatus status,
        String note,
        Instant expiresAt,
        Instant confirmedAt,
        LocalDate interactionDate,
        Integer streakLength
) {
}
