package com.mutuals.event;

import com.mutuals.streak.entity.StreakEndReason;

public record StreakBrokenEvent(Long streakId, Long userAId, Long userBId, int length, StreakEndReason reason) {
}
