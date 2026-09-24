package com.mutuals.social.dto;

import com.mutuals.streak.dto.StreakCardResponse;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record MutualCardResponse(
        Long mutualId,
        UserSummaryResponse friend,
        Instant mutualSince,
        StreakCardResponse streak,
        boolean invitationPending
) {
}
