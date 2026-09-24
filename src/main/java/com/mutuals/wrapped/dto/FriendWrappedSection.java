package com.mutuals.wrapped.dto;

import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;
import java.util.Map;

public record FriendWrappedSection(
        UserSummaryResponse friend,
        boolean currentlyMutual,
        Instant firstMutualAt,
        int totalStreaks,
        int bestStreak,
        long interactions,
        Map<String, Long> interactionsByMethod,
        long challengesCompleted
) {
}
