package com.mutuals.event;


public record AchievementUnlockedEvent(Long userId, Long achievementId) {
}
