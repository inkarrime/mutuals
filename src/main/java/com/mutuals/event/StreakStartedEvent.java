package com.mutuals.event;


public record StreakStartedEvent(Long streakId, Long inviterId, Long inviteeId) {
}
