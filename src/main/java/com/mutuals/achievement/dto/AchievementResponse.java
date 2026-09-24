package com.mutuals.achievement.dto;

import com.mutuals.achievement.entity.AchievementType;

import java.time.Instant;

public record AchievementResponse(
        String code,
        String name,
        String description,
        AchievementType type,
        int threshold,
        int rewardGems,
        String iconUrl,
        boolean unlocked,
        Instant unlockedAt
) {
}
