package com.mutuals.event;


public record StreakMilestoneReachedEvent(Long streakId, Long userAId, Long userBId, int length) {
}
