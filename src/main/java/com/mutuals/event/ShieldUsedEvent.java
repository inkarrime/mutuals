package com.mutuals.event;


public record ShieldUsedEvent(Long streakId, Long userAId, Long userBId, int length) {
}
