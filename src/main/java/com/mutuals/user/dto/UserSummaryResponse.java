package com.mutuals.user.dto;

public record UserSummaryResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        int personalStreak
) {
}
