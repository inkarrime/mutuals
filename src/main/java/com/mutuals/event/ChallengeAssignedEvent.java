package com.mutuals.event;


public record ChallengeAssignedEvent(Long challengeId, Long userAId, Long userBId) {
}
