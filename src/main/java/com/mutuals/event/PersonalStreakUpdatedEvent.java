package com.mutuals.event;


public record PersonalStreakUpdatedEvent(Long userId, int personalStreak) {
}
