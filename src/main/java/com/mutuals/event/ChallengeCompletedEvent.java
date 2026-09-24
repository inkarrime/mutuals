package com.mutuals.event;


public record ChallengeCompletedEvent(Long challengeId, Long userAId, Long userBId, int rewardGems) {
}
