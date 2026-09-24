package com.mutuals.wrapped.dto;

import java.util.Map;

public record UserWrappedStats(
        long interactions,
        long activeDays,
        int longestStreak,
        Map<String, Long> interactionsByMethod,
        String topMutualUsername,
        String topMutualDisplayName,
        long topMutualInteractions,
        long challengesCompleted,
        long gemsEarned,
        long shieldsGifted,
        long newMutuals
) {
}
