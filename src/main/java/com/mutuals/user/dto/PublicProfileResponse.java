package com.mutuals.user.dto;

import java.time.Instant;

public record PublicProfileResponse(
        Long id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        int personalStreak,
        int longestPersonalStreak,
        long followers,
        long following,
        long achievements,
        boolean followedByMe,
        boolean followsMe,
        boolean premium,
        CustomizationResponse customization,
        Instant memberSince
) {
}
