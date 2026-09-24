package com.mutuals.user.dto;

import com.mutuals.user.entity.Role;

import java.util.Set;

public record MeResponse(
        Long id,
        String email,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        Set<Role> roles,
        int gems,
        int shields,
        int personalStreak,
        int longestPersonalStreak,
        boolean premium,
        boolean onboardingCompleted
) {
}
