package com.mutuals.wrapped.dto;

import java.util.Map;

public record PlatformWrappedStats(
        long interactions,
        Map<String, Long> interactionsByMethod,
        int longestStreak,
        String mostActiveDay,
        long mostActiveDayInteractions,
        long activeUsers,
        long activeMutuals,
        long shieldsGifted
) {
}
