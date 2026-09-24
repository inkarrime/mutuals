package com.mutuals.event;


public record StreakAtRiskEvent(Long streakId, Long userAId, Long userBId, int length) {
}
