package com.mutuals.event;


public record ProximityDetectedEvent(Long streakId, Long userAId, Long userBId) {
}
